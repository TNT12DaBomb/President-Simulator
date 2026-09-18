#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p test-build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d test-build ./*.java tests/ConsequenceTests.java tests/VarietyTests.java tests/NewsUITests.java tests/NewsAndAccessTests.java tests/ColorTests.java
for suite in ConsequenceTests VarietyTests NewsAndAccessTests NewsUITests ColorTests; do
 java -Djava.awt.headless=true -cp test-build "$suite"
done
