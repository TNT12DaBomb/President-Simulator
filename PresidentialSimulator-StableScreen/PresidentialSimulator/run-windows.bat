@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
title Presidential Simulator - Interface Playtest
where java >nul 2>nul
if errorlevel 1 (
    echo.
    echo Java was not found. Install Java 17 or newer, then reopen this file.
    echo You do not need to type commands to play.
    echo See START-HERE.txt for help.
    pause
    exit /b 1
)
if exist presidential-simulator.jar (
    java -Dfile.encoding=UTF-8 -jar presidential-simulator.jar --screen %*
) else (
    echo Preparing the game. This first run needs a JDK 17 or newer.
    if not exist build mkdir build
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d build *.java
    if errorlevel 1 (
        echo Could not build the game. Use the included presidential-simulator.jar or install a JDK 17 or newer.
        pause
        exit /b 1
    )
    java -Dfile.encoding=UTF-8 -cp build PresidentialSimulator --screen %*
)
if errorlevel 1 echo The game could not finish. Check that Java 17 or newer is installed.
echo.
echo You can close this window. Open run-windows.bat to play again.
pause
