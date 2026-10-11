# Drawbot Studio

<div align="center">

[![CI](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml/badge.svg)](https://github.com/cid-moosa/drawbot-studio/actions/workflows/ci.yml)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![Design](https://img.shields.io/badge/UI-Apple%20Liquid%20Glass-purple.svg)]()

**A high-performance desktop application for converting raster images into artistic multi-pen vector paths and exporting production-ready G-code, SVG, and vector PDF drawings for pen plotters, CNC drawbots, and polargraphs.**

[Quick Start](#-quick-start) •
[Features](#-key-features) •
[Documentation](#-documentation) •
[Building & Testing](#-building--testing) •
[Contributing](CONTRIBUTING.md) •
[License](LICENSE)

</div>

---

## 🌟 Overview

**Drawbot Studio** bridges the gap between digital photography and physical pen plotting. It transforms digital images into complex, artistic vector drawings using specialized algorithmic pathfinding modules, then exports high-precision machine instructions formatted specifically for drawbots, CNC plotters, laser engravers, and vector illustration workflows.

Modernized, re-architected, and reverse-engineered from Scott Cooper's classic open-source `Drawbot_image_to_gcode_v2` (GPL-3.0).

---

## ✨ Key Features

### 🎨 Apple Liquid Glass User Interface
- **Modern Translucent Design**: Sleek glassmorphism visual hierarchy inspired by Apple Liquid Glass and macOS Sonoma.
- **Dynamic Themes & Accents**: Seamless switching between Dark Mode, Cupertino Dark, Midnight Obsidian, Monterey Light, and High Contrast.
- **Full Workspace Profiles**: Instant switching between **Standard**, **Drawing Focus** (canvas maximized), **Algorithm Tuning**, and **Machine Setup**.
- **Smooth 60 FPS Viewport**: Hardware-accelerated interactive canvas with sub-pixel pan, smooth zoom, 90° rotation, fit-to-view, and paper/origin coordinate grid overlays.

### 🖋️ Artistic Pathfinding Modules (PFMs)
- **Original Squiggles (`PFM_original`)**: Darkness-seeking organic squiggles with 13-angle Bresenham ray sampling and incremental pixel lightening.
- **Modulated Spiral (`PFM_spiral`)**: High-frequency Archimedean spiral with brightness-modulated amplitude and direct pen selection.
- **Wavy Squares (`PFM_squares`)**: 4-way orthogonal geometric cross-hatch with sinusoidal angular modulation.
- **Waveform Scanlines (`PFM_waveform`)**: High-frequency horizontal and vertical sinusoidal scanline waveforms with adjustable amplitude and frequency.

### 🎯 Copic Marker Palette System
- **358 Copic Marker Colors**: Complete catalog with weighted Euclidean color distance matching.
- **18 Curated Marker Sets**: Dark Greys, Warm Greys, Cool Greys, Browns, Reds, Purples, Blues, Yellow-Greens, and custom user palettes.
- **Flexible 6-Pen Distribution**: Even, Monochrome (Sharpie), Lighten curve, Darken curve, and individual pen adjustment.

### 📐 Multi-Format Exporters
- **Production-Ready G-Code**: Metric absolute coordinates (`G21`, `G90`, `G1 Z0/Z1`), customizable pen-up/pen-down heights, feed rates, and boundary test L-brackets.
- **Optimized SVG 1.1**: Inkscape and Illustrator compatible grouped SVG with polyline continuation optimization.
- **Pure Vector PDF 1.4**: Direct vector stroke output without external PDF libraries (both combined documents and individual per-pen PDFs).
- **G-Code Clearance Patch Tool**: Built-in tool to detect and repair incorrect Z-clearance retracts (e.g. converting low `Z1.5` moves to safe `Z30.0` travel).

### ⚡ Performance & Zero Dependencies
- **Compact Segment Buffer**: Reduced heap memory footprint from 80 MB to 18 MB (77.5% reduction) compared to legacy arrays.
- **High Throughput**: Exceeds 18,000 segments/sec (Squiggles) and 2,500,000 segments/sec (Spiral).
- **Zero Third-Party JARs**: Core functionality runs on pure Java SE runtime (`java.awt`, `javax.swing`).

---

## 🚀 Quick Start

### Running Standalone Executable (Windows)

If you downloaded the pre-packaged release, simply run:
```cmd
DrawbotStudio.exe
```
*No Java installation or Processing IDE required.*

### Basic Workflow

1. **Open an Image**: Click **Open Image** or drag-and-drop any JPEG, PNG, or BMP file onto the canvas.
2. **Select an Algorithm**: In the **Algorithm** tab, choose your pathfinding method (`Original Squiggles`, `Modulated Spiral`, `Wavy Squares`, or `Waveform Scanlines`).
3. **Configure Pens**: Adjust contrast, gamma, or choose your marker set in the **Pens** tab.
4. **Generate**: Click **Generate** (or press `Enter`) to watch the drawing trace in real time.
5. **Inspect**: Use toolbar mode selectors:
   - `Drawing`: Full multi-pen vector sketch.
   - `Selected Pen`: Isolate individual pens (0 through 5).
   - `Original Image`: View unmodified source photo.
   - `Reference Image`: View preprocessed, cropped, and filtered image.
   - `Lightened Image`: Inspect progressive brightness decay during path computation.
6. **Export**: Click **Export...** to generate your G-code, SVG, and PDF drawings.

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|:---:|---|
| `[` | Zoom out |
| `]` | Zoom in |
| `\` | Reset zoom, pan, and rotation (Fit to View) |
| `r` | Rotate canvas 90° clockwise |
| `G` | Toggle paper boundaries, grid, and origin indicators |
| `d` | Switch view to final Drawing |
| `O` | Switch view to Original Image |
| `o` | Switch view to Preprocessed Reference |
| `l` | Switch view to Lightened Image |
| `s` | Stop / cancel path generation |
| `g` | Open Multi-Format Export dialog |
| `Ctrl + ,` | Open Preferences & Theme Customizer |

---

## 🛠️ Building & Testing

### Prerequisites
- JDK 17 or higher (OpenJDK / Eclipse Temurin recommended)

### Build and Run Tests
On Windows (PowerShell):
```powershell
.\build_and_test.ps1
```

Or manually:
```bash
# 1. Compile application
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# 2. Compile tests
javac -cp bin -d bin -encoding UTF-8 $(find tests -name "*.java")

# 3. Run automated test suites (7/7 test suites)
java -cp bin com.dullbits.drawbot.tests.AllTestsRunner

# 4. Launch Application
java -cp bin com.dullbits.drawbot.ui.MainWindow
```

For creating native `.exe` installers with `jpackage`, see [BUILDING.md](BUILDING.md).

---

## 📚 Documentation

Detailed engineering and design documentation is available in the [`docs/`](docs/) directory:

- [Reverse Engineering Report](docs/REVERSE_ENGINEERING.md) — Comprehensive analysis of legacy Processing codebase and architectural flaws.
- [Technology Decision](docs/TECHNOLOGY_DECISION.md) — Multi-language evaluation (Processing vs. Rust vs. C++ vs. Modern Java).
- [Architecture Specification](docs/ARCHITECTURE.md) — Decoupled MVC, data pipeline, and thread model.
- [Feature Matrix](docs/FEATURE_MATRIX.md) — 1-to-1 capability comparison matrix.
- [Test Report](docs/TEST_REPORT.md) — Automated regression test results and coverage.
- [Performance Benchmarks](docs/BENCHMARKS.md) — Execution speed and memory profiling metrics.
- [Licensing & Attributions](docs/LICENSING.md) — Complete upstream attribution and copyright notices.

---

## 🤝 Contributing

Contributions are warmly welcomed! Please read our [Contributing Guide](CONTRIBUTING.md) for details on code style, testing requirements, and the pull request workflow.

---

## 📄 License

Drawbot Studio is released under the **GNU General Public License v3.0 (GPL-3.0)**.  
Based on `Drawbot_image_to_gcode_v2` by Scott Cooper (Dullbits.com).  
See [LICENSE](LICENSE) and [docs/LICENSING.md](docs/LICENSING.md) for complete license details.
