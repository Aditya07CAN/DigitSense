@echo off
set "JAVA_HOME=C:\Users\rasen\.tools\jdk-17"
set "PATH=%JAVA_HOME%\bin;C:\Users\rasen\.tools\maven\bin;%PATH%"
cd /d "%~dp0"
echo Starting DigitSense...
call "C:\Users\rasen\.tools\maven\bin\mvn.cmd" exec:java
