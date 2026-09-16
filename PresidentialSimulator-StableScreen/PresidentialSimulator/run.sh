#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
    printf '%s\n' 'Java was not found. Install Java 17 or newer, then try again.'
    exit 1
fi
if [ -f presidential-simulator.jar ]; then
    exec java -Dfile.encoding=UTF-8 -jar presidential-simulator.jar "$@"
fi
mkdir -p build
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d build ./*.java
exec java -Dfile.encoding=UTF-8 -cp build PresidentialSimulator "$@"
