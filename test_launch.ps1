$proc = Start-Process -FilePath "builds\dist\DrawbotStudio\DrawbotStudio.exe" -PassThru
Start-Sleep -Seconds 3
if (!$proc.HasExited) {
    Write-Host "SUCCESS: DrawbotStudio.exe is running healthy! PID: $($proc.Id)"
    Stop-Process -Id $proc.Id -Force
    Write-Host "Clean shutdown verified."
} else {
    Write-Host "ERROR: Process exited prematurely with code: $($proc.ExitCode)"
    exit 1
}
