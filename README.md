# 🌅 Awake

**A completely free, ad-free, native Android alarm app that forces you out of bed.**

Awake is an open-source alternative to Alarmy, designed to be more reliable and far more visually alive. No ads. No paywalls. No subscriptions. Ever.

---

## ✨ Features

- **Disruptive Wake-Up Missions** — Math challenges and Shake challenges that genuinely force you out of bed
- **Bulletproof Reliability** — Uses `AlarmManager.setAlarmClock()` for highest OS priority, foreground service to prevent killing, and boot rescheduling
- **Smooth Volume Ramp-Up** — Starts quiet, ramps to full over 6 seconds (no jarring volume spike)
- **Stunning Dark UI** — Custom design system with animated transitions, haptic feedback, and motion-rich interactions
- **100% Offline** — No accounts, no cloud sync, no server dependency. Works completely offline
- **Zero Tracking** — No analytics, no ads, no telemetry. Your data stays on your device

---

## 📱 Download & Install

### Option 1: Download from GitHub Actions (Recommended)

1. Go to the [Actions](../../actions) tab
2. Click on the latest successful build
3. Download the `awake-debug-apk` artifact
4. Transfer the APK to your Android device
5. Open the APK and allow installation from unknown sources
6. Grant the required permissions when prompted

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/your-username/Awake.git
cd Awake

# Build the debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔐 Release Signing Setup

To build signed release APKs, configure these GitHub Secrets:

1. Go to your repository → Settings → Secrets and variables → Actions
2. Add the following secrets:

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file (`base64 -i keystore.jks \| pbcopy`) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias in the keystore |
| `KEY_PASSWORD` | Key password |

**To generate a keystore:**
```bash
keytool -genkeypair -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias awake
base64 -i release-key.jks > keystore-base64.txt
```

If no secrets are configured, the workflow gracefully falls back to building a debug-signed APK.

---

## 🏗️ Architecture

```
com.awake.alarm/
├── alarm/          # Alarm reliability core
│   ├── AlarmScheduler      # AlarmManager wrapper
│   ├── AlarmBroadcastReceiver  # Fires on alarm trigger
│   ├── AlarmService         # Foreground service for audio
│   ├── BootReceiver         # Reschedules on reboot
│   └── BatteryOptimizationHelper
├── data/           # Room database layer
│   ├── Alarm               # Entity
│   ├── AlarmDao            # Data access
│   ├── AwakeDatabase       # Room database
│   └── AlarmRepository     # Repository pattern
├── mission/        # Pluggable mission system
│   ├── WakeMission         # Interface
│   ├── MathMission         # Math challenge
│   └── ShakeMission        # Shake challenge
├── ui/             # Jetpack Compose UI
│   ├── HomeScreen          # Alarm list
│   ├── CreateAlarmScreen   # Create/edit alarm
│   ├── AlarmRingScreen     # Full-screen mission
│   ├── SettingsScreen      # App settings
│   └── theme/              # Design tokens
└── AwakeApp.kt     # Application class
```

---

## 🎯 Mission System

Awake uses a plugin-style mission architecture. Adding a new mission requires:

1. Implement the `WakeMission` interface
2. Add the mission type to `MissionType` enum
3. Handle the new type in `AlarmRingScreen`

**Current missions:**
- **Math Challenge** — Solve N consecutive math problems (Easy/Medium/Hard/Extreme)
- **Shake Challenge** — Shake your phone vigorously N times (Gentle/Vigorous/Intense)

**Planned missions:**
- Memory Match
- Photo Mission (take a photo of a specific object)
- QR/Barcode Scan

---

## 🛡️ Reliability Features

- **Exact Alarm Scheduling** — Uses `setAlarmClock()` for highest OS priority
- **Foreground Service** — Alarm audio runs in a foreground service, immune to process killing
- **Boot Rescheduling** — All active alarms automatically reschedule after device reboot
- **Battery Optimization Guidance** — One-time prompt to whitelist from battery optimization
- **Volume Ramp-Up** — Smooth 6-second ramp from 20% to target volume
- **Lock Screen Display** — Shows over lock screen, keeps screen on
- **Anti-Dismissal** — Cannot be stopped by swiping away, pressing back, or force-closing

---

## 🚫 What We Don't Do

- ❌ No ads (never)
- ❌ No in-app purchases (never)
- ❌ No subscriptions (never)
- ❌ No user accounts (never)
- ❌ No cloud sync (never)
- ❌ No analytics/tracking (never)
- ❌ No data collection (never)

**Awake is free forever. The source code is open. Your alarm data never leaves your device.**

---

## 📋 Permissions

| Permission | Why |
|------------|-----|
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Fire alarms at exact times |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after reboot |
| `FOREGROUND_SERVICE` | Keep alarm audio playing |
| `WAKE_LOCK` | Keep CPU running during alarm |
| `VIBRATE` | Vibrate on alarm |
| `POST_NOTIFICATIONS` | Show alarm notifications (Android 13+) |

---

## 🛠️ Tech Stack

- **Language:** Kotlin 2.1.20
- **UI:** Jetpack Compose with Material 3
- **Database:** Room
- **Architecture:** Repository pattern
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)

---

## 📄 License

MIT License — do whatever you want with it.

---

## 🙏 Acknowledgments

Inspired by Alarmy's mission-based wake-up approach, but built with:
- No monetization friction
- Dramatically better visual design
- Uncompromising reliability

**Built with 🌅 by the Awake community**
