# CLAUDE.md

Project: Arabic companion app + offline Arabic voice assistant for a **Chinese-market BYD Seal 05 (2025)** head unit, built at the app level only (no firmware changes, no root).

**Start every session by reading `HANDOFF.md`** (current task, connection steps, rules), then `byd-seal05-research-plan.md` (full research, sources, phased plan). Both are in Arabic.

## Hard safety rules for any session connected to the car via ADB
- Read-only commands only until the user explicitly approves a write in the current session. Explain every new adb command before running it.
- Never: `install`/`uninstall`, `pm grant|disable|enable|hide`, `settings put`, `setprop`, `am broadcast|startservice`, `cmd`, `svc`, `reboot`, `remount`, `root`, pushing to system paths, pressing "Reset to factory"/"Reset all settings".
- Never control driving, braking, steering, gear, ADAS/DiPilot, suspension, or HV battery; never inject CAN frames (incl. `com.byd.clusterdebug`) or call `set()` with undocumented feature IDs; never write to cluster/HUD while driving.
- Never install `PackageInstaller.apk` from this repo or replace/disable any system app.
- Never expose ADB (port 5555) to the internet or set up external tunnels.
- If ADB connection fails, stop and report; do not try to enable ADB without the user's explicit decision.
- Never commit IMEI, ICCID, MSISDN, VIN, or serial numbers.
