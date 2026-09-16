@echo off
setlocal
cd /d "%~dp0"
if not exist release-build mkdir release-build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d release-build *.java
if errorlevel 1 (
    echo Build failed. A JDK 17 or newer is required.
    pause
    exit /b 1
)
java -m jdk.jartool/sun.tools.jar.Main --create --file presidential-simulator.jar --main-class PresidentialSimulator -C release-build .
if errorlevel 1 (
    echo Could not package presidential-simulator.jar.
    pause
    exit /b 1
)
echo Build complete. Open run-windows.bat to play your changes.
pause
