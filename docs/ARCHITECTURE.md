# ARCHITECTURE SPECIFICATION: DRAWBOT STUDIO

## 1. System Overview
Drawbot Studio is engineered as a decoupled, multi-threaded desktop application adhering to Clean Architecture principles. Heavy computation (image convolution, ray casting, path generation, and file exporting) runs entirely on asynchronous worker threads, while the graphical user interface (GUI) remains fluid and responsive at 60 FPS.

```
+-----------------------------------------------------------------------+
|                             MAIN WINDOW                               |
|  +--------------------+  +--------------------+  +-----------------+  |
|  |   Toolbar Panel    |  |  Interactive Canvas|  | Control Sidebar |  |
|  | (Open, Run, Export)|  | (Pan/Zoom/Rotate)  |  | (PFM, Pens, Cfg)|  |
|  +--------------------+  +--------------------+  +-----------------+  |
|  +-----------------------------------------------------------------+  |
|  |                       Status & Progress Bar                     |  |
|  +-----------------------------------------------------------------+  |
+-----------------------------------+-----------------------------------+
                                    | Swing Events / Actions
                                    v
+-----------------------------------------------------------------------+
|                    APPLICATION ORCHESTRATION LAYER                    |
|  - ProjectContext (Global Settings, File State, Active Palette)       |
|  - GenerationWorker (Async SwingWorker, Real-Time Progress, Cancel)   |
+-----------------------------------+-----------------------------------+
       |                            |                           |
       v                            v                           v
+---------------+            +------------------+       +---------------+
| IMAGE ENGINE  |            | PATHFINDING (PFM)|       | EXPORT ENGINE |
| - Pipeline    |            | - PfmOriginal    |       | - GcodeExport |
| - Convolution |----------->| - PfmSpiral      |------>| - SvgExport   |
| - Filter Ops  |            | - PfmSquares     |       | - PdfExport   |
| - Vignettes   |            | - Bresenham Ray  |       +---------------+
+---------------+            +------------------+               |
       |                            |                           |
       +----------------------------+---------------------------+
                                    v
                     +------------------------------+
                     |        GEOMETRY CORE         |
                     | - SegmentBuffer (Flat Arrays)|
                     | - DrawingSegment (Primitive) |
                     | - PenDistribution (6 Pens)   |
                     | - CopicPalette (358 Markers) |
                     | - MachineConfig (Dimensions) |
                     +------------------------------+
```

---

## 2. Component Boundaries & Responsibilities

### A. Core Domain & Geometry (`com.dullbits.drawbot.core`)
- **`DrawingSegment`**:
  - Encapsulates a directed 2D line segment: `(x1, y1) -> (x2, y2)`.
  - Attributes: `penNumber` (0–5), `isPenDown` (boolean), `isContinuation` (boolean).
  - Compact memory footprint with primitive float/boolean fields.
- **`SegmentBuffer`**:
  - Dynamically resizable array buffer avoiding boxing.
  - Automatically grows capacity in chunks (initial 64k, doubling up to millions).
  - Replaces the 10,000,000 reference array from the legacy code, cutting memory overhead by over 85%.
  - Computes `setPenContinuationFlags()` in $O(N)$ time.
  - Filters by `displayLineCount` and `selectedPen`.
- **`MachineConfig`**:
  - Configurable paper dimensions (default: 32" x 40" = 812.8 mm x 1016 mm).
  - Configurable printable bounds (default: 28" x 36" = 711.2 mm x 914.4 mm).
  - Configurable pen width (default: 0.65 mm).
  - Configurable origin offset (default: 285 mm from paper top).
  - Coordinate formatting with selectable decimal precision (default: 2 places) and separator.

### B. Color & Pen Allocation (`com.dullbits.drawbot.color`)
- **`CopicPalette`**:
  - Full catalog of 358 Copic sketch markers with RGB hex values and descriptive names.
  - 18 curated marker set presets (Dark Greys, Light Greys, Warm Greys, Cool Greys, Mixed Greys, Browns, Reds, Blues, etc.).
  - Color distance matching using weighted Euclidean distance formula.
- **`PenDistribution`**:
  - Stores distribution percentages for each pen.
  - Quick presets: Even Distribution, Monochrome/Black only.
  - Dynamic adjustments: Lighten (exponential shift toward lighter pens), Darken (shift toward black).
  - Normalization algorithm preventing divide-by-zero errors.

### C. Image Preprocessing Engine (`com.dullbits.drawbot.image`)
- **`ImagePipeline`**:
  - Holds `originalImage` (immutable user input), `preprocessedReference` (cropped, rotated, filtered image to be drawn), and `workingImage` (mutated in real-time during pathfinding as pixels are lightened).
- **`ImageFilters`**:
  - Aspect ratio center cropping to machine area.
  - 90° counter-clockwise auto-rotation for landscape orientation.
  - Resizing to pen-width resolution.
  - Luminance desaturation and average brightness analysis.
- **`ConvolutionEngine`**:
  - 2D kernel convolution with factor and bias offsets.
  - Multi-pass unsharp masking with kernel scaling and normalization.
  - Blur, sharpen, Sobel edge detection, emboss, and motion blur.
- **`BorderFilter`**:
  - Loads border vignettes (`b1.png` through `b11.png`).
  - Inverts, blurs, and blends using additive blending mode.

### D. Pathfinding Modules (`com.dullbits.drawbot.pfm`)
- **`PathFindingModule` Interface**:
  - `void preProcessing(ImagePipeline pipeline, MachineConfig config)`
  - `void findPath(ImagePipeline pipeline, SegmentBuffer buffer, MachineConfig config, ProgressCallback callback)`
  - `void postProcessing(SegmentBuffer buffer, PenDistribution distribution)`
- **`PfmOriginal`**:
  - Darkness-seeking squiggles with 10x downsampled darkest area search and 13-angle Bresenham ray sampling.
- **`PfmSpiral`**:
  - Radiating Archimedean spiral with high-frequency radial jitter modulated by pixel brightness.
- **`PfmSquares`**:
  - Orthogonal wavy squares with sinusoidal angular modulation.

### E. Export Subsystem (`com.dullbits.drawbot.export`)
- **`GcodeExporter`**:
  - Generates per-pen G-code files (`gcode_<name>_pen<p>_<marker>.txt`).
  - Generates boundary test G-code file (`gcode_<name>_test.txt`) with 2-inch L-brackets on all 4 corners.
  - Absolute millimeters (`G21`, `G90`), pen-up (`G1 Z0`), pen-down (`G1 Z1`), formatted coordinate moves.
  - Emits summary header comments (total distance, line count, pen lifts, bounding box).
- **`SvgExporter`**:
  - Grouped `<g id="...">` per pen ordered from lightest to darkest.
  - Combines consecutive contiguous segments into `<polyline points="..." />`.
- **`PdfExporter`**:
  - Pure Java vector PDF generation:
    1. Combined multi-pen PDF document.
    2. Individual PDF documents per active pen.

### F. User Interface & Canvas (`com.dullbits.drawbot.ui`)
- **`MainWindow`**:
  - Main frame with menu bar, top toolbar, split pane layout, canvas, sidebar controls, and status bar.
- **`DrawingCanvas`**:
  - Hardware-accelerated Java2D canvas.
  - Interactive pan (drag), zoom (mouse wheel), rotate (90° steps), fit to window, and reset view.
  - Paper boundary, printable area, and machine origin/center indicators.
  - Real-time rendering with Copic colors and stroke preview.
  - Display modes: Drawing, Single Pen, Original Image, Reference Image, Lightened Image.
- **`GenerationWorker`**:
  - `SwingWorker` thread executing pathfinding outside the event dispatch thread.
  - Streams line count updates and progress percentage to GUI.
  - Clean cancellation flag checked on every squiggle iteration.

---

## 3. Data Flow

```
1. Image Selected -> ImageLoader loads BufferedImage -> ImagePipeline
2. PFM Selected -> Preprocessing runs -> Reference Image generated
3. User Clicks "Generate" -> GenerationWorker spawned in background
4. PFM executes findPath() -> SegmentBuffer populated, Working Image lightened
5. Generation completes/cancels -> PenDistribution applied to SegmentBuffer
6. Canvas repaints vector lines from SegmentBuffer
7. User Clicks "Export" -> Exporters read SegmentBuffer & MachineConfig -> Writes files
```
