@echo off
setlocal
cd /d "%~dp0"
title Presidential Simulator - Governance Edition
where java >nul 2>nul
if errorlevel 1 (
    echo.
    echo Java was not found. Install Java 17 or newer, then reopen this file.
    echo You do not need to type commands to play.
    echo See START-HERE.txt for help.
    pause
    exit /b 1
)
if exist governance.jar (
    java -jar governance.jar %*
) else (
    echo Preparing the game. This first run needs a JDK 17 or newer.
    if not exist build mkdir build
    java -m jdk.compiler/com.sun.tools.javac.Main -d build *.java
    if errorlevel 1 (
        echo Could not build the game. Use the included governance.jar or install a JDK 17 or newer.
        pause
        exit /b 1
    )
    java -cp build PresidentialSimulator %*
)
if errorlevel 1 echo The game could not finish. Check that Java 17 or newer is installed.
echo.
echo You can close this window. Open run-windows.bat to play again.
pause
