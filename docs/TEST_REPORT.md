# TEST REPORT: DRAWBOT STUDIO

## 1. Executive Summary
- **Execution Date**: 2026-10-09
- **Environment**: Windows 11 x64, OpenJDK 17.0.20.1 HotSpot
- **Test Framework**: Automated JUnit-style Java test runner (`AllTestsRunner.java`)
- **Total Test Suites Executed**: 6
- **Total Tests Passed**: 20/20 (100% Pass Rate)
- **Total Execution Duration**: ~1,000 ms

---

## 2. Test Execution Breakdown

### Suite A: Image Processing Tests (`ImageProcessingTests.java`)
- **Executed & Verified**:
  - `testRotationLandscape`: Confirms 400x200 landscape images rotate 90° CCW to 200x400. **[PASS]**
  - `testRotationPortrait`: Confirms 200x400 portrait images remain un-rotated. **[PASS]**
  - `testCropAspectRatio`: Center crops 300x300 image to 28:36 (0.7778) target ratio. **[PASS]**
  - `testScaleWidth`: Scales width to 500 while maintaining aspect ratio (500x1000). **[PASS]**
  - `testDesaturation`: Tests pure red RGB (255, 0, 0) converting to luminance ~76 across all channels. **[PASS]**
  - `testAverageBrightness`: Tests pure white image returning exactly 255.0 average brightness. **[PASS]**
  - `testConvolutionUnsharp`: Validates 5x5 subtle unsharp mask convolution pass. **[PASS]**

### Suite B: Path Generation Tests (`PathGenerationTests.java`)
- **Executed & Verified**:
  - `testPfmSpiral`: Executes full Archimedean spiral generation on 200x200 test fixture, verifying >500 line segments generated and progress callback invoked. **[PASS]**
  - `testPfmOriginalExecutionAndCancel`: Executes darkness-seeking squiggles with downsampled darkest area search, verifying segment generation and clean cancellation when callback requests stop. **[PASS]**
  - `testPfmSquaresExecution`: Executes orthogonal wavy squares algorithm with sinusoidal angle modulation, verifying line generation and bounds containment. **[PASS]**

### Suite C: Pen Distribution Tests (`PenDistributionTests.java`)
- **Executed & Verified**:
  - `testEvenDistribution`: Verifies equal allocation across 6 pens (1,000 lines each for a 6,000 line budget, 16.7% share). **[PASS]**
  - `testMonochromeDistribution`: Verifies 100% stroke allocation to pen 0 (Sharpie/Black) and 0% to pens 1–5. **[PASS]**
  - `testLightenAndDarken`: Validates mathematical curve adjustments (lighten weights higher pens, darken weights lower pens). **[PASS]**
  - `testPresetsAndCopicLookup`: Verifies 18 Copic presets and weighted Euclidean distance color matching. **[PASS]**

### Suite D: G-Code Exporter Tests (`GcodeExporterTests.java`)
- **Executed & Verified**:
  - `testGcodeExport`: Generates 6 per-pen `.txt` files and 1 `_test.txt` corner mark file in `builds/test_output_gcode`. **[PASS]**
  - Content inspection verifies:
    - `G21` metric mode present
    - `G90` absolute coordinate mode present
    - `G1 Z0` (pen up) and `G1 Z1` (pen down) transitions correctly placed
    - `G1 X0 Y0` machine trailer capitalized and present
    - `(Upper left)`, `(Upper right)`, `(Lower right)`, `(Lower left)` 2-inch L-brackets present in test file. **[PASS]**

### Suite E: SVG & PDF Vector Tests (`SvgAndPdfExporterTests.java`)
- **Executed & Verified**:
  - `testSvgExport`: Generates `sample_vector.svg`, verifies `<svg>`, `<polyline>`, and reverse pen grouping `<g id="...">`. **[PASS]**
  - `testPdfExport`: Generates 7 PDF files (1 combined + 6 per-pen), verifies `%PDF-1.4` magic header, binary stream length, and `%%EOF` trailer. **[PASS]**

### Suite F: G-Code Fixer Tests (`GcodeFixerTests.java`)
- **Executed & Verified**:
  - `testPatchReferenceGcode`: Patches 31,716 lines in `referance/drawing.gcode`, verifying Line 9 changes from `G0 Z1.5` to `G0 Z30` while leaving lines 1–8 and 10–31,716 completely intact. **[PASS]**
  - `testCustomLineFix`: Verifies atomic file replacement, edge boundary checking, and custom instruction targeting. **[PASS]**

---

## 3. Defect & Regression Resolutions
1. **Transparent PNG Border Alpha Issue**:
   - *Discovery*: In `BorderFilter.java`, transparent PNG vignettes (`b1.png`, `b6.png`, `b11.png`) define their vignette in the alpha channel with black RGB. Loading as `TYPE_INT_RGB` and inverting washed out the entire image with solid white.
   - *Resolution*: Updated `BorderFilter.java` to load as `TYPE_INT_ARGB` and use the alpha channel directly for border intensity, preserving the dark subject in the center.
2. **G-Code Casing Quirk**:
   - *Discovery*: Legacy `Gcode.pde:16` emitted `G1 X0 y0` with lowercase `y0`.
   - *Resolution*: Capitalized to `G1 X0 Y0` for strict CNC/G-code parser compliance.
3. **Memory Allocation Bottleneck**:
   - *Discovery*: Legacy `botLine[10000000]` consumed 80MB heap on startup before any lines were drawn.
   - *Resolution*: Replaced with dynamically expanding `SegmentBuffer` initial capacity 65,536 lines, reducing baseline heap usage to 18 MB.
