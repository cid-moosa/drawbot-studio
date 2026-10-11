# Feature Matrix: Drawbot Studio

This matrix records every original feature from `Drawbot_image_to_gcode_v2`, its source implementation, its modernized implementation location in Drawbot Studio, and its automated/manual verification test.

| Feature Category | Original Feature Description | Original Source Implementation | Drawbot Studio Location | Verification Test |
|---|---|---|---|---|
| **Image Input** | Load image file from local disk (JPEG, PNG, BMP) | `fileSelected(File)` in `Drawbot_image_to_gcode_v2.pde:178` | `ImageLoader.java` / `WorkspacePanel.java` | `ImageLoaderTest.testLoadValidImage()` |
| **Image Input** | Retain original image intact separately from working buffers | `img_orginal` in `Drawbot_image_to_gcode_v2.pde:210` | `ProjectContext.java (originalImage)` | `ImagePipelineTest.testOriginalBufferPreservation()` |
| **Image Input** | Graceful error handling for corrupt/unsupported images | Crashes or exits `exit()` | `ImageLoader.java` with user dialog & logging | `ImageLoaderTest.testCorruptImageHandling()` |
| **Image Input** | Explicit URL image download with timeout/size limit | `GClip.paste()` regex test on startup (`Drawbot_image_to_gcode_v2.pde:116`) | `UrlImageDownloader.java` with explicit dialog & size/timeout checks | `UrlDownloaderTest.testDownloadValidation()` |
| **Image Input** | Drag-and-drop image file import | Not supported | `DrawingCanvas.java` / `FileDropHandler.java` | GUI manual & event simulation test |
| **Preprocessing** | Auto-rotate landscape images 90° CCW | `image_rotate()` in `Image_Tools.pde:63` | `ImageFilters.rotate90CCW()` | `ImageFiltersTest.testImageRotate()` |
| **Preprocessing** | Aspect ratio center cropping to machine area (28:36) | `image_crop()` in `Image_Tools.pde:119` | `ImageFilters.cropToRatio()` | `ImageFiltersTest.testAspectCrop()` |
| **Preprocessing** | Resizing / scaling to pen resolution | `image_scale()` in `Image_Tools.pde:99` | `ImageFilters.scaleWidth()` | `ImageFiltersTest.testImageScale()` |
| **Preprocessing** | Grayscale desaturation | `image_desaturate()` in `Image_Tools.pde:8` | `ImageFilters.desaturate()` | `ImageFiltersTest.testDesaturate()` |
| **Preprocessing** | 5x5 Unsharp masking convolution | `image_unsharpen()` in `Image_Tools.pde:173` | `ConvolutionEngine.unsharpen()` | `ConvolutionTest.testUnsharpMask()` |
| **Preprocessing** | Vignette/Border overlay with blur & add blend | `image_boarder()` in `Image_Tools.pde:148` | `BorderFilter.applyBorder()` | `BorderFilterTest.testBorderBlending()` |
| **Preprocessing** | Average image brightness calculation | `avg_imgage_brightness()` in `Image_Tools.pde:108` | `ImageAnalysis.averageBrightness()` | `ImageAnalysisTest.testBrightness()` |
| **Algorithms** | PFM 0: Original darkness-seeking squiggles | `PFM_original.pde:6` | `PfmOriginal.java` | `AlgorithmTest.testPfmOriginal()` |
| **Algorithms** | PFM 1: Modulated Archimedean Spiral | `PFM_spiral.pde:8` | `PfmSpiral.java` | `AlgorithmTest.testPfmSpiral()` |
| **Algorithms** | PFM 2: Orthogonal wavy squares | `PFM_squares.pde:5` | `PfmSquares.java` | `AlgorithmTest.testPfmSquares()` |
| **Algorithms** | Dynamic Bresenham ray sampling | `bresenham()` in `Bresenham.pde:16` | `Bresenham.java` | `BresenhamTest.testBresenhamRay()` |
| **Algorithms** | Pixel lightening along drawing paths | `bresenham_lighten()` in `Bresenham.pde:97` | `PfmUtils.lightenPath()` | `BresenhamTest.testPixelLighten()` |
| **Drawing Engine**| Compact line segment storage | `botLine[] lines = new botLine[10000000]` in `BotDrawing.pde:5` | `SegmentBuffer.java` (compact flat primitive structs) | `SegmentBufferTest.testCapacityAndMemory()` |
| **Drawing Engine**| Pen up / pen down transition tracking | `is_pen_down` in `Gcode.pde:26-34` | `DrawingSegment.java` | `SegmentBufferTest.testPenTransitions()` |
| **Drawing Engine**| Pen continuation flag calculation | `set_pen_continuation_flags()` in `BotDrawing.pde:74` | `SegmentBuffer.computeContinuations()` | `SegmentBufferTest.testContinuations()` |
| **Pen Management**| 18 Copic marker palette sets | `copic_sets` in `Drawbot_image_to_gcode_v2.pde:75` | `CopicPalette.PRESETS` in `CopicPalette.java` | `CopicTest.testPresetsAvailable()` |
| **Pen Management**| 358 Copic marker color/name lookup & distance | `Copix` in `Copic.pde:57` | `CopicPalette.java` | `CopicTest.testClosestMarker()` |
| **Pen Management**| Even pen distribution | `set_even_distribution()` in `Drawbot_image_to_gcode_v2.pde:415` | `PenDistribution.setEven()` | `PenDistributionTest.testEvenDistribution()` |
| **Pen Management**| Black/first pen only distribution | `set_black_distribution()` in `Drawbot_image_to_gcode_v2.pde:424` | `PenDistribution.setMonochrome()` | `PenDistributionTest.testMonochrome()` |
| **Pen Management**| Configurable pen weighting (lighten / darken / individual) | Keybindings `1-0`, `shift 1-0`, `9`, `0` in `Drawbot_image_to_gcode_v2.pde:306-359` | `PenDistributionPanel.java` graphical sliders & normalizer | `PenDistributionTest.testCustomNormalization()` |
| **Visualization**| Interactive Canvas pan, zoom, rotate (90° steps) | Keys `[`, `]`, `r`, `\`, mouse drag in `Drawbot_image_to_gcode_v2.pde:304,388,493` | `DrawingCanvas.java` with mouse wheel zoom, drag pan, toolbar rotate & reset | `CanvasTransformTest.testTransformMatrix()` |
| **Visualization**| Display mode switching (Drawing, Pen-only, Original, Reference, Lightened) | Keys `d`, `ctrl 1-6`, `O`, `o`, `l` in `Drawbot_image_to_gcode_v2.pde:289-302` | `ViewToolbar.java` & Canvas layer controls | GUI interaction test |
| **Visualization**| Paper, printable area, and machine origin grid | `grid()` in `Misc.pde:16` | `DrawingCanvas.paintGrid()` | `CanvasGridTest.testBoundsDimensions()` |
| **Visualization**| Stroke budget limit adjustment | Keys `<`, `>` in `Drawbot_image_to_gcode_v2.pde:369-380` | `StrokeBudgetSlider.java` | `SegmentBufferTest.testDisplayStrokeLimit()` |
| **Export** | Per-pen machine G-code files | `create_gcode_files()` in `Gcode.pde:56` | `GcodeExporter.exportPenGcode()` | `GcodeExporterTest.testPenGcodeFiles()` |
| **Export** | Bounding box test G-code file | `create_gcode_test_file()` in `Gcode.pde:139` | `GcodeExporter.exportBoundsTest()` | `GcodeExporterTest.testBoundsGcode()` |
| **Export** | Combined SVG export with grouped polyline optimization | `create_svg_file()` in `Gcode.pde:199` | `SvgExporter.exportSvg()` | `SvgExporterTest.testSvgValidPolylines()` |
| **Export** | Combined multi-pen vector PDF export | `render_to_pdf()` in `BotDrawing.pde:38` | `PdfExporter.exportCombinedPdf()` | `PdfExporterTest.testCombinedPdfStructure()` |
| **Export** | Individual per-pen vector PDF exports | `render_each_pen_to_pdf()` in `BotDrawing.pde:55` | `PdfExporter.exportPerPenPdf()` | `PdfExporterTest.testPerPenPdfFiles()` |
| **Async / UI** | Non-blocking background worker with progress & cancellation | Blocking single thread in Processing `draw()` loop | `GenerationWorker.java` (SwingWorker / Background Task) | `WorkerTest.testCancellationAndProgress()` |
| **Packaging** | Standalone Windows x64 executable & portable distribution | None (required Processing IDE 3.5.4) | `jpackage` standalone image (`DrawbotStudio.exe`) | Execution verification in clean environment |
