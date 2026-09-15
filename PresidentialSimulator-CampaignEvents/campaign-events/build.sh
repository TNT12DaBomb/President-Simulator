#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p release-build
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d release-build ./*.java
java -m jdk.jartool/sun.tools.jar.Main --create --file campaign.jar --main-class PresidentialSimulator -C release-build .
printf '%s\n' 'Build complete. Run sh run.sh to play your changes.'
