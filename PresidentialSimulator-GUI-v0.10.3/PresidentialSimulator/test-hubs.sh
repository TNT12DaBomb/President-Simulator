#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p test-build docs
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -Xlint:all -d test-build ./*.java tests/DeskHubTests.java
java -Djava.awt.headless=true -cp test-build DeskHubTests
