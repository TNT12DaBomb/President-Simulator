#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
rm -rf test-build
mkdir -p test-build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d test-build ./*.java tests/*.java
for suite in EngineTests DebateTests WorldTests InterfaceTests StableScreenTests DesktopTests MapDesktopTests PolishTests RefinedTests ExpansionTests; do
    java -cp test-build "$suite"
done
