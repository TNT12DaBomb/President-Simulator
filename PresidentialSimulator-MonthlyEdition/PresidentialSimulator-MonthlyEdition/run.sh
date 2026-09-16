#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
    printf '%s\n' 'Java was not found. Install Java 17 or newer, then try again.'
    exit 1
fi
if [ -f monthly.jar ]; then
    exec java -jar monthly.jar "$@"
fi
mkdir -p build
java -m jdk.compiler/com.sun.tools.javac.Main -d build ./*.java
exec java -cp build PresidentialSimulator "$@"
