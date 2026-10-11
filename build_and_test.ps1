param (
    [switch]$WithVisualCaptures
)

$ErrorActionPreference = "Stop"

Write-Host "--- Compiling Application Classes ---"
$src = Get-ChildItem -Recurse -Filter "*.java" -Path "src" | ForEach-Object { $_.FullName }
javac -d bin -encoding UTF-8 $src
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Source compilation successful."

Write-Host "--- Compiling Test Classes ---"
$test = Get-ChildItem -Recurse -Filter "*.java" -Path "tests" | ForEach-Object { $_.FullName }
javac -cp bin -d bin -encoding UTF-8 $test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Test compilation successful."

Write-Host "--- Running All Automated Tests ---"
java "-Djava.awt.headless=true" -cp bin com.dullbits.drawbot.tests.AllTestsRunner
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($WithVisualCaptures) {
    Write-Host "--- Running Visual UI Captures ---"
    java -cp bin com.dullbits.drawbot.tests.CaptureMainWindow
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host "Visual UI Captures generated successfully."
}
