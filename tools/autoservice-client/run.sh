#!/usr/bin/env bash
# Pushes AutoServiceClient to the connected car and runs it via app_process
# as the shell user — the same access level already proven (section 8.12)
# to reach autoservice's raw Binder interface with no permission wall.
#
# Unlike ReadTest.java (2026-10-10 session) this tool needs NO Context and
# NO CarSetting.apk on the classpath: it only touches android.os.Parcel/
# IBinder/Binder (base framework) plus one reflective ServiceManager.getService
# lookup. That's why main() can run directly with no Looper/ActivityThread setup.
#
# Usage: ./run.sh <code> [i32:<n> | binder] ...
# Example (confirmed getInt — driver window %):
#   ./run.sh 5 i32:1001 i32:947912728
#
# Safety: this script does not decide what's safe to call — you do. Read
# byd-seal05-research-plan.md sections 8.11-8.13 before passing any args
# shaped like a write (3+ int args, or a "binder" arg).
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f out/classes.dex ]; then
  echo "out/classes.dex not found — run ./build.sh first"
  exit 1
fi

REMOTE_DEX=/data/local/tmp/autoserviceclient.dex

adb push out/classes.dex "$REMOTE_DEX" >/dev/null
adb shell "CLASSPATH=$REMOTE_DEX app_process /data/local/tmp AutoServiceClient $*"
adb shell rm -f "$REMOTE_DEX"
