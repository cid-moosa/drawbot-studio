# Drawbot Studio

<div align="center">

[![CI](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml/badge.svg)](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()

**A desktop application for converting raster images into multi-pen vector paths and exporting G-code, SVG, and vector PDF drawings for pen plotters and CNC drawbots.**

[Quick Start](#quick-start) •
[Features](#features) •
[Building & Testing](#building--testing) •
[Documentation](#documentation) •
[Contributing](CONTRIBUTING.md) •
[License](LICENSE)

</div>

---

## Overview

**Drawbot Studio** converts raster images into artistic multi-pen vector paths using algorithmic pathfinding modules, then exports machine-ready G-code, SVG, and PDF drawings for pen plotters, CNC drawbots, and polargraphs.

Based on Scott Cooper's open-source `Drawbot_image_to_gcode_v2` (GPL-3.0).

---

## Features

### Pathfinding Modules (PFMs)
- **Original Squiggles (`PFM_original`)**: Darkness-seeking organic squiggles with 13-angle Bresenham ray sampling and pixel lightening.
- **Modulated Spiral (`PFM_spiral`)**: Archimedean spiral with brightness-modulated amplitude.
- **Wavy Squares (`PFM_squares`)**: 4-way orthogonal cross-hatching with angular modulation.
- **Waveform Scanlines (`PFM_waveform`)**: Horizontal and vertical sinusoidal scanline waveforms with adjustable amplitude and frequency.

### Palette & Pen Controls
- **Copic Marker Palette**: Complete catalog of 358 Copic marker colors with Euclidean distance matching.
- **18 Presets**: Dark greys, warm greys, cool greys, browns, reds, purples, blues, yellow-greens, etc.
- **6-Pen Distribution**: Even, monochrome (single pen), lighten curve, darken curve, and manual per-pen adjustments.

### Exporters
- **G-Code**: Metric absolute coordinates (`G21`, `G90`, `G1 Z0/Z1`), configurable pen-up/pen-down heights, feed rates, and corner boundary test lines.
- **SVG**: Grouped SVG 1.1 with polyline continuation.
- **Vector PDF**: Multi-pen vector PDF 1.4 output without external dependencies.
- **G-Code Patch Tool**: Utility to adjust Z retract clearance across existing G-code files.

### Interface & Controls
- **Dark and Light Themes**: Configurable dark, light, and high-contrast themes with custom accent colors.
- **Workspace Modes**: Standard, drawing focus (maximized canvas), algorithm tuning, and machine setup layouts.
- **Interactive Canvas**: Smooth pan, zoom, 90° rotation, fit-to-view, and coordinate grid overlays.

---

## Quick Start (Plug & Play)

### Running on Windows
Choose any of the following (all run immediately with zero setup):

- **Option A (1-Click Launcher)**: Double-click `Run_Drawbot_Studio.bat` (or `Launch.bat`) in the project root.
- **Option B (Direct Standalone Executable)**: Open `builds\dist\DrawbotStudio\` and double-click `DrawbotStudio.exe`.
- **Option C (Portable ZIP Package)**: Download or extract `builds\DrawbotStudio-v1.0.0-Windows-x64.zip` and run `DrawbotStudio.exe`.

*No installation, environment variables, or Java setup required. The application includes its own optimized runtime.*

### Basic Workflow
1. **Open Image**: Click **Open Image** or drag and drop an image onto the canvas.
2. **Choose Algorithm**: Select a pathfinding module in the **Algorithm** tab.
3. **Configure Pens**: Set your pen palette and brightness thresholds in the **Pens** tab.
4. **Generate**: Click **Generate** (or press Enter) to trace paths.
5. **Export**: Click **Export...** or **Export SVG** to save G-code, SVG, or PDF files.

---

## Keyboard Shortcuts

| Shortcut | Action |
|:---:|---|
| `[` | Zoom out |
| `]` | Zoom in |
| `\` | Fit to view |
| `r` | Rotate canvas 90° clockwise |
| `G` | Toggle grid and origin guides |
| `d` | Switch view to final drawing |
| `O` | Switch view to original image |
| `o` | Switch view to reference image |
| `l` | Switch view to lightened image |
| `s` | Stop path generation |
| `g` | Open export dialog |
| `Ctrl + ,` | Open preferences dialog |

---

## Building & Testing

### Requirements
- JDK 17 or higher

### Build & Run Tests
On Windows (PowerShell):
```powershell
.\build_and_test.ps1
```

Or manually:
```bash
# Compile application
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Compile tests
javac -cp bin -d bin -encoding UTF-8 $(find tests -name "*.java")

# Run tests
java -cp bin com.dullbits.drawbot.tests.AllTestsRunner

# Run application
java -cp bin com.dullbits.drawbot.ui.MainWindow
```

For creating native `.exe` installers with `jpackage`, see [BUILDING.md](BUILDING.md).

---

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Feature Matrix](docs/FEATURE_MATRIX.md)
- [Reverse Engineering Report](docs/REVERSE_ENGINEERING.md)
- [Benchmarks](docs/BENCHMARKS.md)
- [Test Report](docs/TEST_REPORT.md)
- [Licensing & Attribution](docs/LICENSING.md)

---

## License

Drawbot Studio is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.  
Based on `Drawbot_image_to_gcode_v2` by Scott Cooper (Dullbits.com).  
See [LICENSE](LICENSE) and [docs/LICENSING.md](docs/LICENSING.md).
