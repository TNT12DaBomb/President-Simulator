#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
java -m jdk.compiler/com.sun.tools.javac.Main -d build ./*.java
java -cp build PresidentialSimulator "$@"
