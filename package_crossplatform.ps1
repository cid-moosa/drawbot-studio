# package_crossplatform.ps1
# Creates plug-and-play distribution packages for Windows, macOS, Linux, and Universal JAR.

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }

Write-Host "=== Packaging Cross-Platform Releases ===" -ForegroundColor Cyan

# 1. Universal Executable JAR
$jarSource = Join-Path $root "builds\DrawbotStudio.jar"
$universalJar = Join-Path $root "builds\DrawbotStudio-v1.0.0-Universal.jar"
if (-not (Test-Path $jarSource)) {
    throw "DrawbotStudio.jar not found at $jarSource"
}
Copy-Item -Path $jarSource -Destination $universalJar -Force
Write-Host "[OK] Universal JAR: builds/DrawbotStudio-v1.0.0-Universal.jar ($( [math]::Round((Get-Item $universalJar).Length / 1MB, 2) ) MB)" -ForegroundColor Green

# 2. macOS Package
$distMac = Join-Path $root "builds\dist_mac\DrawbotStudio"
if (Test-Path $distMac) {
    # Remove existing staging folder safely
    [System.IO.Directory]::Delete($distMac, $true)
}
New-Item -ItemType Directory -Path $distMac -Force | Out-Null
Copy-Item -Path $jarSource -Destination (Join-Path $distMac "DrawbotStudio.jar") -Force
Copy-Item -Path (Join-Path $root "assets") -Destination (Join-Path $distMac "assets") -Recurse -Force

# Write DrawbotStudio.command with Unix LF
$macCommand = @'
#!/usr/bin/env bash
# Drawbot Studio - macOS Plug & Play Launcher
cd "$(dirname "$0")" || exit 1

if ! command -v java &>/dev/null; then
    osascript -e 'display alert "Java 17+ Required" message "Java was not found. Please install OpenJDK 17 or later (e.g. from https://adoptium.net or with brew install openjdk@17) to launch Drawbot Studio."'
    exit 1
fi

exec java -Xms256m -Xmx2048m \
    -XX:+UseG1GC \
    -Xdock:name="Drawbot Studio" \
    -Xdock:icon="assets/icon.png" \
    -Dapple.awt.application.name="Drawbot Studio" \
    -Dapple.laf.useScreenMenuBar=true \
    -jar "DrawbotStudio.jar" "$@"
'@ -replace "`r`n", "`n"
[System.IO.File]::WriteAllText((Join-Path $distMac "DrawbotStudio.command"), $macCommand, (New-Object System.Text.UTF8Encoding($false)))

$macRunSh = @'
#!/usr/bin/env bash
cd "$(dirname "$0")" || exit 1
exec java -Xms256m -Xmx2048m -Xdock:name="Drawbot Studio" -jar "DrawbotStudio.jar" "$@"
'@ -replace "`r`n", "`n"
[System.IO.File]::WriteAllText((Join-Path $distMac "run.sh"), $macRunSh, (New-Object System.Text.UTF8Encoding($false)))

$macHowTo = @'
================================================================================
DRAWBOT STUDIO v1.0.0 — MACOS QUICKSTART (PLUG & PLAY)
================================================================================

PREREQUISITE:
  Java 17 or higher is required.
  Verify with: java -version
  If needed, install via Homebrew:
    brew install openjdk@17
  Or download standard installer from:
    https://adoptium.net

HOW TO LAUNCH:
  Option 1 (Easiest - Double Click):
    Double-click 'DrawbotStudio.command'

  Option 2 (Terminal):
    Open Terminal in this folder and run:
      chmod +x DrawbotStudio.command run.sh
      ./DrawbotStudio.command

GATEKEEPER / PERMISSION NOTE:
  If macOS flags the script as downloaded from the internet:
    Right-click 'DrawbotStudio.command' -> Open -> Click Open.
  Or run in Terminal:
    xattr -cr .

GETTING STARTED:
  1. Drag & drop any image (PNG, JPG, BMP) or click File -> Open Image.
  2. Select your drawing algorithm and style preset.
  3. Click '▶ Generate' to preview toolpaths in real-time.
  4. Click 'Export G-code' or 'Export SVG' to export ready-to-plot files.
================================================================================
'@
[System.IO.File]::WriteAllText((Join-Path $distMac "HOW_TO_RUN.txt"), $macHowTo, (New-Object System.Text.UTF8Encoding($false)))

# Archive macOS
$macZip = Join-Path $root "builds\DrawbotStudio-v1.0.0-macOS.zip"
if (Test-Path $macZip) { Remove-Item $macZip -Force }
$tarMacCmd = "tar.exe -a -cf `"$macZip`" -C `"$root\builds\dist_mac`" DrawbotStudio"
cmd /c $tarMacCmd
Write-Host "[OK] macOS Package: builds/DrawbotStudio-v1.0.0-macOS.zip ($( [math]::Round((Get-Item $macZip).Length / 1MB, 2) ) MB)" -ForegroundColor Green

# 3. Linux Package
$distLinux = Join-Path $root "builds\dist_linux\DrawbotStudio"
if (Test-Path $distLinux) {
    [System.IO.Directory]::Delete($distLinux, $true)
}
New-Item -ItemType Directory -Path $distLinux -Force | Out-Null
Copy-Item -Path $jarSource -Destination (Join-Path $distLinux "DrawbotStudio.jar") -Force
Copy-Item -Path (Join-Path $root "assets") -Destination (Join-Path $distLinux "assets") -Recurse -Force

$linuxRunSh = @'
#!/usr/bin/env bash
# Drawbot Studio - Linux Plug & Play Launcher
cd "$(dirname "$0")" || exit 1

if ! command -v java &>/dev/null; then
    echo "ERROR: Java 17+ is required but not found in PATH."
    echo "Install via your package manager:"
    echo "  Ubuntu/Debian : sudo apt install openjdk-17-jre"
    echo "  Fedora/RHEL   : sudo dnf install java-17-openjdk"
    echo "  Arch Linux    : sudo pacman -S jre17-openjdk"
    exit 1
fi

exec java -Xms256m -Xmx2048m \
    -XX:+UseG1GC \
    -Dawt.useSystemAAFontSettings=on \
    -Dswing.aatext=true \
    -jar "DrawbotStudio.jar" "$@"
'@ -replace "`r`n", "`n"
[System.IO.File]::WriteAllText((Join-Path $distLinux "run.sh"), $linuxRunSh, (New-Object System.Text.UTF8Encoding($false)))

$linuxDesktop = @'
[Desktop Entry]
Version=1.0
Type=Application
Name=Drawbot Studio
Comment=Artistic Multi-Pen Toolpath & G-code Generator
Exec=bash -c "cd $(dirname %k) && ./run.sh"
Icon=assets/icon.png
Terminal=false
Categories=Graphics;2DGraphics;RasterGraphics;
StartupNotify=true
'@ -replace "`r`n", "`n"
[System.IO.File]::WriteAllText((Join-Path $distLinux "drawbot-studio.desktop"), $linuxDesktop, (New-Object System.Text.UTF8Encoding($false)))

$linuxHowTo = @'
================================================================================
DRAWBOT STUDIO v1.0.0 — LINUX QUICKSTART (PLUG & PLAY)
================================================================================

PREREQUISITE:
  Java 17 or higher is required.
  Verify with: java -version
  If needed, install using your package manager:
    Ubuntu / Debian : sudo apt update && sudo apt install openjdk-17-jre
    Fedora / RHEL   : sudo dnf install java-17-openjdk
    Arch Linux      : sudo pacman -S jre17-openjdk

HOW TO LAUNCH:
  Option 1 (One-Click Launcher):
    chmod +x run.sh
    ./run.sh

  Option 2 (Direct JAR):
    java -jar DrawbotStudio.jar

  Option 3 (Desktop Launcher):
    chmod +x drawbot-studio.desktop
    Double-click 'drawbot-studio.desktop' (select 'Allow Launching' if prompted).

GETTING STARTED:
  1. Drag & drop any image (PNG, JPG, BMP) or click File -> Open Image.
  2. Select your drawing algorithm and style preset.
  3. Click '▶ Generate' to preview toolpaths in real-time.
  4. Click 'Export G-code' or 'Export SVG' to export ready-to-plot files.
================================================================================
'@
[System.IO.File]::WriteAllText((Join-Path $distLinux "HOW_TO_RUN.txt"), $linuxHowTo, (New-Object System.Text.UTF8Encoding($false)))

# Archive Linux (.tar.gz)
$linuxTar = Join-Path $root "builds\DrawbotStudio-v1.0.0-Linux.tar.gz"
if (Test-Path $linuxTar) { Remove-Item $linuxTar -Force }
$tarLinuxCmd = "tar.exe -czf `"$linuxTar`" -C `"$root\builds\dist_linux`" DrawbotStudio"
cmd /c $tarLinuxCmd
Write-Host "[OK] Linux Package: builds/DrawbotStudio-v1.0.0-Linux.tar.gz ($( [math]::Round((Get-Item $linuxTar).Length / 1MB, 2) ) MB)" -ForegroundColor Green

Write-Host "=== All Cross-Platform Release Packages Ready! ===" -ForegroundColor Cyan
