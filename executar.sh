#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 --release 17 -cp 'lib/*' -d out src/*.java
java -cp 'out:lib/*' Sistema
