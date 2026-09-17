@echo off
setlocal
cd /d "%~dp0"
where javaw >nul 2>nul
if errorlevel 1 (
    echo Install Java 17 or newer, then reopen this launcher.
    pause
    exit /b 1
)
if not exist presidential-simulator.jar (
    echo Build the source with build-windows.bat first.
    pause
    exit /b 1
)
start "" javaw -Dfile.encoding=UTF-8 -jar presidential-simulator.jar
