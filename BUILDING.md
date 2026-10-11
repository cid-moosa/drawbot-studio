# BUILDING & PACKAGING GUIDE: DRAWBOT STUDIO

This document provides step-by-step instructions for building, testing, benchmarking, and packaging **Drawbot Studio** into a standalone Windows x64 executable.

---

## 1. Prerequisites
- **Operating System**: Windows 10 or Windows 11 (64-bit)
- **JDK**: OpenJDK 17 or later (Temurin, Corretto, Oracle, etc.)
- Tools included in JDK: `javac`, `jar`, `jlink`, `jpackage`

Verify your tools:
```powershell
javac -version
jar --version
jpackage --version
```

---

## 2. Compile Source Code

Compile all application classes to `bin/`:
```powershell
New-Item -ItemType Directory -Force -Path "bin"
$srcFiles = Get-ChildItem -Recurse -Filter "*.java" -Path "src" | ForEach-Object { $_.FullName }
javac -d "bin" -encoding UTF-8 $srcFiles
```

---

## 3. Run Automated Tests

Compile and run the 5 automated test suites:
```powershell
$testFiles = Get-ChildItem -Recurse -Filter "*.java" -Path "tests" | ForEach-Object { $_.FullName }
javac -cp "bin" -d "bin" -encoding UTF-8 $testFiles
java -cp "bin" com.dullbits.drawbot.tests.AllTestsRunner
```

Expected output:
```
RESULT: SUCCESS — 5/5 Test Suites Passed in ~8,400 ms
```

---

## 4. Run Performance Benchmarks

Profile image loading, preprocessing, pathfinding throughput, and exporter latency:
```powershell
java -cp "bin" com.dullbits.drawbot.tests.BenchmarkRunner
```

---

## 5. Build Executable Application JAR

Create `builds/DrawbotStudio.jar`:
```powershell
New-Item -ItemType Directory -Force -Path "builds"
jar --create --file "builds\DrawbotStudio.jar" --main-class "com.dullbits.drawbot.DrawbotStudio" -C "bin" .
```

To run directly from JAR:
```powershell
java -jar "builds\DrawbotStudio.jar"
```

---

## 6. Build Standalone Windows x64 Executable (`.exe`)

Use `jpackage` to bundle a self-contained portable distribution with a minimal embedded JRE:
```powershell
New-Item -ItemType Directory -Force -Path "builds\dist"
jpackage --type app-image `
  --name "DrawbotStudio" `
  --app-version "1.0.0" `
  --vendor "DrawbotStudio" `
  --icon "assets/icon.ico" `
  --input "builds" `
  --main-jar "DrawbotStudio.jar" `
  --main-class "com.dullbits.drawbot.DrawbotStudio" `
  --dest "builds/dist" `
  --java-options "-Xmx2048m -Dsun.java2d.d3d=true -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true"

# Copy assets folder alongside the binary
Copy-Item -Path "assets" -Destination "builds\dist\DrawbotStudio\assets" -Recurse -Force
```

The resulting folder:
```
builds/dist/DrawbotStudio/
  ├── DrawbotStudio.exe    <-- Double-click to launch (no Java install required!)
  ├── app/
  ├── runtime/             <-- Stripped, lightweight modular JRE
  └── assets/              <-- Vignette borders and icons
```

---

## 7. Build Windows Installer (MSI / EXE)

To build a Windows Installer package:
```powershell
jpackage --type exe `
  --name "DrawbotStudio-Installer" `
  --app-image "builds\dist\DrawbotStudio" `
  --dest "builds\installer" `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser
```
*(Note: Requires WiX Toolset 3.x installed on the system for MSI/EXE generation).*
