@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
java -m jdk.compiler/com.sun.tools.javac.Main -d build *.java
if errorlevel 1 (
    echo Compilation failed. Install a JDK 17 or newer and ensure java is on PATH.
    pause
    exit /b 1
)
java -cp build PresidentialSimulator %*
pause
