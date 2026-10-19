# Drawbot Studio

<div align="center">

[![CI](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml/badge.svg)](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()

**A desktop application for converting raster images into multi-pen vector paths and exporting G-code, SVG, and vector PDF drawings for pen plotters and CNC drawbots.**

[Downloads](#downloads--cross-platform-releases-plug--play) •
[Quick Start](#quick-start-guide) •
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

## Downloads & Cross-Platform Releases (Plug & Play)

Pre-built plug-and-play releases are available for Windows, macOS, Linux, and universal cross-platform:

| Platform | Package | Size | Quick Start |
|---|---|---|---|
| **Windows (x64)** | [DrawbotStudio-v1.0.0-Windows-x64.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Windows-x64.zip) | 73.9 MB | **Zero install.** Extract & double-click `DrawbotStudio.exe` (Bundled OpenJDK 17 runtime included) |
| **macOS (Intel & Apple Silicon)** | [DrawbotStudio-v1.0.0-macOS.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-macOS.zip) | 26.0 MB | Extract & double-click `DrawbotStudio.command` (or run `./run.sh`) |
| **Linux (x64 / ARM)** | [DrawbotStudio-v1.0.0-Linux.tar.gz](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Linux.tar.gz) | 25.9 MB | Extract & run `./run.sh` or double-click `drawbot-studio.desktop` |
| **Universal (Any OS with Java 17+)** | [DrawbotStudio-v1.0.0-Universal.jar](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Universal.jar) | 13.2 MB | 1-file download. Double-click or run `java -jar DrawbotStudio-v1.0.0-Universal.jar` |

---

## Quick Start Guide

### Windows (x64)
- **Portable ZIP**: Download [DrawbotStudio-v1.0.0-Windows-x64.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Windows-x64.zip), extract it, and double-click `DrawbotStudio.exe`.
- **Repository Clone**: Double-click `Run_Drawbot_Studio.bat` (or `Launch.bat`) in the project root.
- *No installation or Java setup required. Includes self-contained OpenJDK 17 runtime.*

### macOS (Intel & Apple Silicon)
1. Ensure Java 17+ is installed (`java -version` or `brew install openjdk@17`).
2. Download [DrawbotStudio-v1.0.0-macOS.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-macOS.zip) and extract it.
3. Double-click `DrawbotStudio.command` (or in Terminal: `chmod +x DrawbotStudio.command run.sh && ./DrawbotStudio.command`).
   *(If prompted by Gatekeeper, right-click `DrawbotStudio.command` -> Open -> Open, or run `xattr -cr .`).*

### Linux (Ubuntu, Debian, Fedora, Arch)
1. Ensure Java 17+ is installed (`sudo apt install openjdk-17-jre` or `sudo dnf install java-17-openjdk`).
2. Download [DrawbotStudio-v1.0.0-Linux.tar.gz](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Linux.tar.gz) and extract:
   ```bash
   tar -xzf DrawbotStudio-v1.0.0-Linux.tar.gz
   cd DrawbotStudio
   chmod +x run.sh
   ./run.sh
   ```
   *(Or double-click `drawbot-studio.desktop`).*

### Universal Executable JAR
1. Download [DrawbotStudio-v1.0.0-Universal.jar](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Universal.jar).
2. Double-click the file, or run from command line on any system:
   ```bash
   java -jar DrawbotStudio-v1.0.0-Universal.jar
   ```

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
