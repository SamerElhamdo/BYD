#!/usr/bin/env bash
# Compiles AutoServiceClient.java to a dex, matching the exact toolchain
# used for ReadTest.java in the 2026-10-11 session (javac -> d8).
# Does NOT touch the car. Safe to run anytime, offline.
set -euo pipefail
cd "$(dirname "$0")"

ANDROID_JAR="${ANDROID_JAR:-/var/home/samer-dev/.android-sdk/platforms/android-28/android.jar}"
D8="${D8:-/var/home/samer-dev/.android-sdk/build-tools/34.0.0/d8}"

rm -rf out
mkdir -p out
javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -cp "$ANDROID_JAR" -d out AutoServiceClient.java
"$D8" --output out out/AutoServiceClient.class

echo "Built: out/classes.dex"
