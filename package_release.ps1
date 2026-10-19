$ErrorActionPreference = "Stop"
cd $PSScriptRoot

Write-Host "--- 1. Compiling Latest Classes ---"
$src = Get-ChildItem -Recurse -Filter "*.java" -Path "src" | ForEach-Object { $_.FullName }
javac -d bin -encoding UTF-8 $src
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "--- 2. Building Master JAR ---"
jar cfe builds/DrawbotStudio.jar com.dullbits.drawbot.DrawbotStudio -C bin .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "--- 3. Updating Dist App Bundle ---"
Copy-Item -Path "builds/DrawbotStudio.jar" -Destination "builds/dist/DrawbotStudio/app/DrawbotStudio.jar" -Force

Write-Host "--- 4. Creating Plug & Play Release Archive ---"
$zipPath = "builds/DrawbotStudio-v1.0.0-Windows-x64.zip"
if (Test-Path $zipPath) {
    Remove-Item -Path $zipPath -Force
}

tar.exe -a -cf $zipPath --exclude="bench_outputs" --exclude="dist" --exclude="*.log" -C "builds/dist" "DrawbotStudio"
if ($LASTEXITCODE -eq 0) {
    $size = (Get-Item $zipPath).Length / 1MB
    Write-Host ("Release archive ready: {0} ({1:N1} MB)" -f $zipPath, $size)
} else {
    Write-Host "Tar command failed with exit code $LASTEXITCODE"
}
