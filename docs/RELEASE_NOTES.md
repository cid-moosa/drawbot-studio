## Drawbot Studio v1.0.0 — Cross-Platform Release (Plug & Play)

Standalone desktop application for converting raster images into artistic multi-pen vector paths and exporting machine-ready G-code, SVG, and PDF drawings for pen plotters and CNC drawbots.

---

### Plug & Play Downloads

| Platform | Package | Size | Quick Start |
|---|---|---|---|
| **Windows (x64)** | [DrawbotStudio-v1.0.0-Windows-x64.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Windows-x64.zip) | 73.9 MB | **Zero install.** Extract & double-click `DrawbotStudio.exe` (Bundled OpenJDK 17 runtime included) |
| **macOS (Intel & Apple Silicon)** | [DrawbotStudio-v1.0.0-macOS.zip](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-macOS.zip) | 26.0 MB | Extract & double-click `DrawbotStudio.command` (or run `./run.sh`) |
| **Linux (x64 / ARM)** | [DrawbotStudio-v1.0.0-Linux.tar.gz](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Linux.tar.gz) | 25.9 MB | Extract & run `./run.sh` or double-click `drawbot-studio.desktop` |
| **Universal (Any OS with Java 17+)** | [DrawbotStudio-v1.0.0-Universal.jar](https://github.com/cid-moosa/drawbot-studio/releases/download/v1.0.0/DrawbotStudio-v1.0.0-Universal.jar) | 13.2 MB | 1-file download. Double-click or run `java -jar DrawbotStudio-v1.0.0-Universal.jar` |

---

### Platform Instructions

#### Windows (x64)
1. Download **`DrawbotStudio-v1.0.0-Windows-x64.zip`** and extract it anywhere.
2. Double-click **`DrawbotStudio.exe`** (or `Run_Drawbot_Studio.bat`).
3. No installation or Java setup needed.

#### macOS (Intel / M1 / M2 / M3 / M4)
1. Prerequisite: Java 17+ installed (`java -version` or `brew install openjdk@17`).
2. Download **`DrawbotStudio-v1.0.0-macOS.zip`** and extract it.
3. Double-click **`DrawbotStudio.command`**.
   *(If prompted by Gatekeeper, right-click `DrawbotStudio.command` -> Open -> Open, or run `xattr -cr .` in Terminal).*

#### Linux (Ubuntu / Debian / Fedora / Arch)
1. Prerequisite: Java 17+ installed (`sudo apt install openjdk-17-jre` or `sudo dnf install java-17-openjdk`).
2. Download **`DrawbotStudio-v1.0.0-Linux.tar.gz`** and extract: `tar -xzf DrawbotStudio-v1.0.0-Linux.tar.gz`.
3. Open terminal in the folder and run:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```
   *(Or double-click `drawbot-studio.desktop`).*

#### Universal Executable JAR
1. Download **`DrawbotStudio-v1.0.0-Universal.jar`**.
2. Double-click the file, or launch from command line:
   ```bash
   java -jar DrawbotStudio-v1.0.0-Universal.jar
   ```

---

### Highlights & Capabilities
- **High Performance**: Hardware-accelerated canvas with `Path2D.Float` polyline batching for smooth 60 FPS viewport navigation.
- **4 Pathfinding Modules**: Original Squiggles (Bresenham ray sampling), Modulated Spiral, Wavy Squares, and Waveform Scanlines.
- **Full Copic Palette**: 358 colors and 18 curated presets with configurable 6-pen distribution.
- **Multi-Format Export**: Production-ready metric G-code (`G21`, `G90`), grouped SVG 1.1, and vector PDF 1.4.
- **Customizable Interface**: Dark, light, and high-contrast themes with responsive workspace profiles.
