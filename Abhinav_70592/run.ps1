$env:JAVA_HOME = "C:\Users\rasen\.tools\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\rasen\.tools\maven\bin;$env:PATH"
Set-Location $PSScriptRoot
Write-Host "Starting DigitSense..." -ForegroundColor Cyan
& "C:\Users\rasen\.tools\maven\bin\mvn.cmd" exec:java
