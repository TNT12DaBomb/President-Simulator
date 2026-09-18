#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p test-build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d test-build ./*.java tests/MonthFlowTests.java
java -Djava.awt.headless=true -cp test-build MonthFlowTests
