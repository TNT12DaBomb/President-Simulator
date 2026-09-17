#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p release-build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d release-build ./*.java
java -m jdk.jartool/sun.tools.jar.Main --create --file presidential-simulator.jar --main-class DesktopLauncher -C release-build .
printf '%s\n' 'Build complete. Run sh run.sh to play your changes.'
