@echo off
rem ============================================================
rem  Drawbot Studio — 1-Click Plug & Play Launcher
rem ============================================================
cd /d "%~dp0"

if exist "builds\dist\DrawbotStudio\DrawbotStudio.exe" (
    start "" "builds\dist\DrawbotStudio\DrawbotStudio.exe" %*
    exit /b 0
)

if exist "builds\DrawbotStudio.jar" (
    start "" javaw -Xms256m -Xmx2048m -Dsun.java2d.d3d=true -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -jar "builds\DrawbotStudio.jar" %*
    exit /b 0
)

if exist "bin\com\dullbits\drawbot\DrawbotStudio.class" (
    start "" javaw -Xms256m -Xmx2048m -Dsun.java2d.d3d=true -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -cp bin com.dullbits.drawbot.DrawbotStudio %*
    exit /b 0
)

echo Initializing and compiling Drawbot Studio...
powershell -ExecutionPolicy Bypass -File build_and_test.ps1
if exist "builds\dist\DrawbotStudio\DrawbotStudio.exe" (
    start "" "builds\dist\DrawbotStudio\DrawbotStudio.exe" %*
    exit /b 0
)
if exist "bin\com\dullbits\drawbot\DrawbotStudio.class" (
    start "" javaw -Xms256m -Xmx2048m -Dsun.java2d.d3d=true -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -cp bin com.dullbits.drawbot.DrawbotStudio %*
    exit /b 0
)

pause
