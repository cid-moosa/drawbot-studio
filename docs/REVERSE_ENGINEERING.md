# REVERSE ENGINEERING REPORT: Drawbot_image_to_gcode_v2

## 1. Executive Summary
The project `Drawbot_image_to_gcode_v2` (v3.75 beta by Scott Cooper, Dullbits.com) is a Processing-based application designed to convert bitmap images into artistic line paths formatted for multi-pen vertical drawing machines (drawbots, polargraphs, pen plotters) and vector outputs (G-code, SVG, PDF).

The software was developed primarily around a multi-pen physical plotting workflow utilizing Copic sketch markers with varying gray/color values (e.g., 6 shades ranging from 100 Black to N2 Ultra-Light Gray).

---

## 2. Source-Code Inventory & File Roles

| File | Lines | Primary Responsibility |
|---|---|---|
| `Drawbot_image_to_gcode_v2.pde` | 511 | Application lifecycle, setup, draw loop, global state machine, keyboard events, mouse navigation, dynamic class loading of PFMs, clipboard URL detection, and pen distribution normalization. |
| `BotDrawing.pde` | 133 | Container class `botDrawing` holding drawing segments (`botLine[] lines = new botLine[10000000]`), line count, PDF rendering (combined & per-pen), pen continuation flag calculation, and pen distribution assignment. |
| `BotLine.pde` | 42 | Value object `botLine` representing a single directed 2D line segment (`x1, y1, x2, y2`), pen state (`pen_down`), pen assignment (`pen_number`), and continuation status. Contains Processing `render_with_copic()` call using `MULTIPLY` blend mode. |
| `Bresenham.pde` | 106 | Integer ray and line rasterization implementations: `bresenham(x0, y0, x1, y1)` generating point lists, `midpoint_circle(x0, y0, radius)`, and `bresenham_lighten` which incrementally lightens pixels along a path. |
| `Copic.pde` | 719 | `Copix` color management class with lookup tables for 358 Copic marker codes, hex colors, and descriptive names. Implements Euclidean weighted RGB color distance calculation (`0.30*(r2-r1)^2 + 0.59*(g2-g1)^2 + 0.11*(b2-b1)^2`). |
| `GClip.java` | 119 | Java AWT clipboard helper for copying and pasting string data to check for image URLs. |
| `Gcode.pde` | 261 | Exporter routines: `create_gcode_files` (per pen), `create_gcode_test_file` (corner brackets), `create_svg_file` (polyline optimized XML), coordinate scaling, offset translation, and machine formatting (`G21`, `G90`, `G1 Z0/Z1`). |
| `Image_Tools.pde` | 457 | Image manipulation routines: aspect ratio center crop (`image_crop`), rotate 90° CCW if landscape (`image_rotate`), scaling, brightness average, borders/vignettes with Gaussian blur (`image_boarder`), and 2D convolution filters (unsharp mask, Sobel, blur, sharpen, emboss). |
| `Misc.pde` | 110 | `Limit` tracking class for min/max coordinates, interactive `grid()` overlay drawing paper boundary, machine origins, and `mouse_point()` coordinate readout. |
| `PFM_original.pde` | 202 | Core artistic algorithm: iterative darkest area search, Bresenham ray sampling across 13 angles, segment walking, and pixel lightening. |
| `PFM_spiral.pde` | 116 | Spiral algorithm: Archimedean spiral radiating outward with radial jitter and pen selection driven by local pixel brightness. |
| `PFM_squares.pde` | 160 | Wavy square squiggle algorithm: 4-way orthogonal directional sampling with sinusoidal angle modulation and brightness-based lightening. |
| `CHANGELOG.md` | 58 | Version history from v3.70 to v3.75. |
| `README.md` | 70 | Keybinding cheatsheet and overview. |
| `LICENSE` | 674 | GNU General Public License v3.0. |

---

## 3. Application Lifecycle & State Machine

The original program is driven by an integer state machine:

```
[State 1: Selection] 
        |  (URL from clipboard or selectInput file dialog)
        v
[State 2: Setup]
        |  Load image, rotate if landscape, copy original,
        |  run PFM pre_processing(), calculate G-code & screen scale
        v
[State 3: Pathfinding]
        |  Repeated calls to pfm.find_path()
        |  Generates botLine segments, lightens image
        |  Terminates when avg brightness > 250 or user presses 's'
        v
[State 4: Post-Processing]
        |  pfm.post_processing(), calculate pen distributions,
        |  assign pen numbers across line segments
        v
[State 5: Rendering / Export]
        |  Display render_all(), pan/zoom navigation,
        |  'g' key triggers G-code, SVG, and PDF file generation
```

---

## 4. Image Preprocessing Pipeline

1. **Orientation Normalization (`image_rotate`)**:
   - If `width > height`, rotates image 90 degrees counter-clockwise to align the long axis vertically with the drawbot's printable area.
2. **Aspect Ratio Center Cropping (`image_crop`)**:
   - Target ratio: `image_size_x / image_size_y` = (28 in * 25.4) / (36 in * 25.4) = 28 / 36 = 0.77778.
   - Crops excess width or height symmetrically from the center.
3. **Scaling (`image_scale`)**:
   - `PFM_original`: Scaled to `int(image_size_x / pen_width)` = `(28 * 25.4) / 0.65` = 1094 pixels width.
   - `PFM_spiral` & `PFM_squares`: Scaled to fixed 1000 pixels width.
4. **Convolution Filtering**:
   - Multi-pass unsharp masking using a 5x5 matrix scaled by an amount factor and normalized.
5. **Border / Vignette Blending (`image_boarder`)**:
   - Loads a grayscale border image from `boarder/` (e.g. `b1.png`, `b6.png`, `b11.png`), resizes to image dimensions, inverts, blurs, and blends onto the target image using `ADD` mode.
6. **Grayscale Desaturation (`image_desaturate`)**:
   - Converts pixels to standard luminance grayscale (`GRAY`).

---

## 5. Pathfinding Modules (PFMs)

### A. PFM_original (Darkness-Seeking Squiggles)
- **Concept**: Simulates an organic, continuous hand-drawn sketch by finding dark regions, tracing short squiggles through the darkest neighboring paths, and lightening drawn pixels so lines don't continuously re-trace the same spot.
- **Darkest Root**:
  - Downsamples image by 10x (`area_size = 10`).
  - Finds the minimum brightness pixel in the downsampled image.
  - Adds random jitter within that 10x10 block.
- **Squiggle Walk**:
  - Walks `squiggle_length = 500` iterations before lifting the pen.
  - At each step, casts `tests = 13` Bresenham rays of length `random(3, 40)` spread over 360 degrees (or directional angles when `squiggle_count >= 190`).
  - Finds the ray with lowest average brightness.
  - Moves pen to endpoint, lightens all pixels along the ray by `adjustbrightness * 5` (where `adjustbrightness = 10`).
  - Loops until average overall image brightness exceeds `250.0`.

### B. PFM_spiral (Modulated Archimedean Spiral)
- **Concept**: Draws a continuous spiral starting from the center and spiraling out to the corners.
- **Modulation**:
  - Distance between rings: `dist = 7` mm.
  - At each polar step, radius fluctuates based on local pixel brightness (`b = map(brightness, 0, 255, dist*ampScale, 0)`).
  - Radii alternate (`xa, ya` and `xb, yb`), creating a high-frequency zigzag/wavy ring whose amplitude is inversely proportional to brightness (dark regions produce wide zigzags; bright regions produce thin lines).
  - Pen is lifted if pixel brightness exceeds `mask` threshold (240).
  - Pen number is directly mapped: `int(map(brightness, 0, 255, 0, pen_count-1) + 0.5)`.

### C. PFM_squares (Wavy Orthogonal Cross-Hatch)
- **Concept**: Similar to `PFM_original`, but limits ray casting tests to `tests = 4` orthogonal/diagonal angles with a fixed step length of 30 pixels.
- **Angle Modulation**:
  - Start angle modulated sinusoidally: `36 + sin(x/9 + 46) + cos(y/26 + 26)`.
  - Produces stylized, geometric, wavy square hatch marks.

---

## 6. Drawing Representation & Pen Distribution

- Coordinates are initially stored in pixel space (`x1, y1, x2, y2`).
- Each segment has a boolean `pen_down` and integer `pen_number` (0 to 5).
- Pen distribution logic:
  - Total line segments are counted (`display_line_count`).
  - Default: Evenly distributed across active pens.
  - User can weight distribution: lighten (shift towards higher pen indices / lighter pens) or darken (shift towards lower pen indices / black).
  - In `distribute_pen_changes_according_to_percentages`, segments are assigned sequential pen numbers so each pen draws its assigned percentage of the total stroke budget.

---

## 7. Machine Coordinate Space & G-Code Generation

### Machine Geometry Defaults:
- Paper dimensions: 32 x 40 inches (812.8 mm x 1016.0 mm).
- Printable area: 28 x 36 inches (711.2 mm x 914.4 mm).
- Pen width: 0.65 mm.
- Paper top to origin: 285 mm.
- Machine home / origin is centered horizontally and offset vertically:
  $$\text{gcode\_scale} = \min\left(\frac{711.2}{\text{img.width}}, \frac{914.4}{\text{img.height}}\right)$$
  $$\text{gcode\_offset\_x} = -\frac{\text{img.width} \times \text{gcode\_scale}}{2.0}$$
  $$\text{gcode\_offset\_y} = -\left(285.0 - \frac{1016.0 - (\text{img.height} \times \text{gcode\_scale})}{2.0}\right)$$

### Output Formats:
1. **Per-Pen G-Code (`.txt`)**:
   - `G21` (millimeters), `G90` (absolute positioning).
   - Pen up: `G1 Z0`.
   - Rapid traverse: `G1 X{x} Y{y}`.
   - Pen down: `G1 Z1`.
   - Linear feed movements with coordinate formatting to `gcode_decimals` (2 decimal places).
   - Trailer moves to origin and lifts pen.
2. **Boundary Test G-Code (`_test.txt`)**:
   - Draws 2-inch L-marks at all 4 bounding corners (`dx.min, dy.min`, `dx.max, dy.max`) to allow the operator to verify physical paper alignment before plotting.
3. **Combined SVG (`.svg`)**:
   - Groups `<g id="{copic_code}">` for each pen in reverse order (light pens rendered first, black last).
   - Combines consecutive connected segments into `<polyline points="..." />`.
4. **Vector PDF (`.pdf`)**:
   - Single combined multi-color PDF.
   - Individual PDF files per pen (`_pen0_100.pdf`, etc.).

---

## 8. Deficiencies, Architectural Bottlenecks, and Risks in Original Code

1. **Catastrophic Memory Waste**:
   - `botLine[] lines = new botLine[10000000];` preallocates a 10-million reference array regardless of drawing size. Each `botLine` is a separate Java heap object, causing severe garbage collection overhead.
2. **UI Thread Blocking**:
   - Path generation and image processing execute directly inside Processing's `draw()` loop on the main rendering thread. Processing completely freezes during large convolutions, and cancellation is unresponsive.
3. **Obsolete Processing 3 Dependencies**:
   - Relies on Processing 3.5.4 Java 8 binaries and outdated JOGL OpenGL bindings that fail on modern Windows 11 drivers.
4. **Hard-coded Keyboard Shortcuts**:
   - No graphical sliders, dropdowns, or buttons. All adjustments (pen distribution, zoom, export, PFM selection) require memorizing obscure single-key shortcuts.
5. **Lack of Image File Dialog & Drag-and-Drop**:
   - Relies on modal AWT dialog or blindly attempting to fetch URLs from the system clipboard on startup (which crashes if the clipboard contains non-image text).
6. **Off-by-One / Array Index Bugs**:
   - In `distribute_pen_changes_according_to_percentages`, an off-by-one error triggers an error log and hacky clamp.
   - Lines array is 1-indexed (`lines[1]` to `lines[line_count]`), which violates standard programming idioms.
7. **Trailer Syntax Typo**:
   - Line 16 of `Gcode.pde` emits `G1 X0 y0` (lowercase `y`), which violates standard Fanuc/RepRap G-code capitalization.
