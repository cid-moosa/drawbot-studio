# Contributing to Drawbot Studio

Thank you for your interest in contributing to **Drawbot Studio**!

Drawbot Studio is an open-source, high-performance desktop application for converting raster images into artistic multi-pen vector paths and exporting production-ready G-code, SVG, and vector PDF drawings for pen plotters, CNC drawbots, and polargraphs.

---

## Code of Conduct

We are committed to providing a welcoming, inclusive, and harassment-free experience for everyone. Please be respectful and constructive in all issues, pull requests, and discussions.

---

## How Can I Contribute?

- **Report Bugs**: If you find an issue, submit a detailed report on GitHub Issues including steps to reproduce, sample images/G-code, OS version, and console logs.
- **Suggest Features**: Share ideas for new artistic pathfinding modules (PFMs), export formats, or UI enhancements.
- **Submit Code**: Pick up an open issue or propose an improvement via a Pull Request.
- **Improve Documentation**: Enhance guides, tooltips, or reverse engineering notes.

---

## Development Setup

### Prerequisites

- **Java Development Kit (JDK) 17 or higher** (OpenJDK / Eclipse Temurin recommended).
- **Git**.
- **PowerShell** (on Windows) or Bash (on Linux/macOS).

No third-party dependency managers (Maven/Gradle) or IDE plugins are required — Drawbot Studio is engineered to compile directly with standard `javac`.

### Clone the Repository

```bash
git clone https://github.com/cid-moosa/drawbot-studio.git
cd drawbot-studio
```

### Compiling and Running Tests

On Windows (PowerShell):
```powershell
.\build_and_test.ps1
```

Or manually:
```bash
# Compile application classes
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Compile test classes
javac -cp bin -d bin -encoding UTF-8 $(find tests -name "*.java")

# Run all automated test suites
java -cp bin com.dullbits.drawbot.tests.AllTestsRunner
```

### Launching the Application

```bash
java -cp bin com.dullbits.drawbot.ui.MainWindow
```

---

## Architecture & Code Guidelines

1. **Zero External Dependencies**: Core functionality relies exclusively on the standard Java SE runtime (`java.awt`, `javax.swing`, `java.awt.image`). Avoid adding heavyweight third-party JAR dependencies.
2. **Liquid Glass Design System**: UI styling is centralized in `DrawbotTheme` and `DrawbotPreferences`. Custom Swing components should support both dark and light modes, smooth anti-aliased borders, and responsive layouts.
3. **Thread Safety**: Long-running image processing and path generation must execute on background threads via `SwingWorker` or `CompletableFuture`, keeping the UI responsive at 60 FPS.
4. **Memory Efficiency**: Never allocate multi-million element fixed arrays on the heap. Use dynamic, primitive-backed buffers like `SegmentBuffer`.
5. **G-Code Compliance**: Generated G-code must be standard metric absolute (`G21`, `G90`) and compatible with GRBL, Marlin, and custom drawbot controllers.

---

## Pull Request Process

1. Fork the repository and create your branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. Make your changes adhering to existing conventions.
3. Add unit or regression tests in `tests/com/dullbits/drawbot/tests/` where applicable.
4. Verify all tests pass locally:
   ```powershell
   .\build_and_test.ps1
   ```
5. Commit your changes using conventional commit messages:
   - `feat: add new spiral density parameter`
   - `fix: resolve Z clearance retract in GcodeExporter`
   - `docs: update feature matrix with Waveform PFM`
6. Push to your fork and submit a Pull Request against `main`.
7. Ensure all GitHub Actions CI checks pass.

---

## License

By contributing to Drawbot Studio, you agree that your contributions will be licensed under its **GNU General Public License v3.0 (GPL-3.0)**.
