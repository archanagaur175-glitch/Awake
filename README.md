# Awake

**A free, ad-free, native Android alarm app with wake-up missions.**

Awake is an open-source alarm app built around one idea: you complete a mission
before the alarm stops. No ads, no paywalls, no subscriptions, no accounts.

## Features

- **Wake-up missions** — Math and Shake challenges with several difficulty levels.
- **Exact scheduling** — `AlarmManager.setAlarmClock()` for high OS priority.
- **Foreground alarm service** — audio runs in a `mediaPlayback` foreground service.
- **Boot rescheduling** — active alarms are restored after reboot and after app
  updates.
- **Volume ramp-up** — starts quiet and ramps to the target volume over 6 seconds.
- **Offline and local** — no accounts, no cloud sync, no server dependency, no
  analytics. Alarm data stays on the device.
- **Custom dark Compose UI** — a bespoke design system with animated transitions and
  haptic feedback.

## Download and build

### From CI

1. Open the **Actions** tab.
2. Select the latest successful build.
3. Download the `awake-debug-apk` artifact.
4. Transfer the APK to the device and allow installation from unknown sources.
5. Grant the requested permissions.

### From source

```bash
./gradlew assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

> **No Gradle wrapper is tracked.** `./gradlew` will not exist on a clean clone.
> CI invokes a preinstalled `gradle` binary, so either install Gradle locally or
> generate the wrapper before building. Use `gradle assembleDebug` if Gradle is on
> your PATH.

## Release signing

Signed release APKs are optional and CI-only. Configure these repository secrets:

| Secret | Description |
| --- | --- |
| `KEYSTORE_BASE64` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias in the keystore |
| `KEY_PASSWORD` | Key password |

`release.yml` runs on `v*` tags or manual dispatch and builds a signed release APK
only when the keystore secrets exist. Without them, the build workflow produces a
debug APK.

## Architecture

```
com.awake.alarm/
├── alarm/          Alarm reliability core
│   ├── AlarmScheduler             AlarmManager wrapper
│   ├── AlarmBroadcastReceiver     Fires on alarm trigger
│   ├── AlarmService               Foreground service for audio
│   ├── BootReceiver               Reschedules on reboot
│   └── BatteryOptimizationHelper
├── data/           Room database layer
│   ├── Alarm                      Entity
│   ├── AlarmDao                   Data access
│   ├── AwakeDatabase              Room database
│   └── AlarmRepository            Repository pattern
├── mission/        Pluggable mission system
│   ├── WakeMission                Interface
│   ├── MathMission                Math challenge
│   └── ShakeMission               Shake challenge
├── ui/             Jetpack Compose UI
│   ├── HomeScreen                 Alarm list
│   ├── CreateAlarmScreen          Create/edit alarm
│   ├── AlarmRingScreen            Full-screen mission
│   ├── SettingsScreen             App settings
│   └── theme/                     Design tokens
└── AwakeApp.kt     Application class
```

Navigation has four destinations: `home` (start), `create`, `edit/{alarmId}` and
`settings`. `AlarmRingActivity` is a separate activity shown over the lock screen,
with `singleInstance` set and excluded from recents.

## Mission system

The contract is the `WakeMission` interface: `start()`, `isComplete()`,
`onDismiss()`, `render()`, `progress()` and `statusText()`.

**Implemented missions:**

- **Math** — solve N consecutive problems. Easy: 3 correct additions/subtractions up
  to 20. Medium: 5 up to 50. Hard: 7 including multiplication up to 100. Extreme:
  10 up to 500.
- **Shake** — shake N times. Gentle: 12, Vigorous: 20 (default), Intense: 28.

Memory Match, Photo and QR/Barcode exist only as enum placeholders, not as
implemented missions.

Adding a mission requires implementing `WakeMission`, adding it to `MissionType`,
and handling it in `AlarmRingScreen`.

## Reliability features

- `setAlarmClock()` scheduling for the alarm trigger.
- A foreground service of type `mediaPlayback` for audio.
- `AlarmBroadcastReceiver` handles `com.awake.alarm.ACTION_ALARM_FIRED`;
  `BootReceiver` handles `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`.
- Wake lock, notification channel and vibration start in the service.
- A one-time battery-optimization guidance prompt.
- Six-second volume ramp.
- The ringing activity shows over the lock screen and keeps the screen on.

The lock-screen and foreground-service handling is present in code. A guarantee that
the alarm "cannot be stopped by force-closing" is not established by the code and is
not claimed here.

## Permissions

Nine permissions are declared:

| Permission | Why |
| --- | --- |
| `SCHEDULE_EXACT_ALARM` (max SDK 32), `USE_EXACT_ALARM` | Fire alarms at exact times |
| `RECEIVE_BOOT_COMPLETED` | Reschedule after reboot |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Keep alarm audio playing |
| `WAKE_LOCK` | Keep the CPU running during an alarm |
| `VIBRATE` | Vibrate on alarm |
| `POST_NOTIFICATIONS` | Show alarm notifications on Android 13+ |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Battery-optimization guidance |

## Data

Room database `AwakeDatabase`, version 1, `exportSchema = false`, with one entity:

`alarms`: auto-generated id, hour, minute, enabled, label, `daysOfWeek` list,
`missionType`, `missionConfigJson`, volume and `createdAt`.

`daysOfWeek` uses 1 = Monday through 7 = Sunday; an empty list means a one-shot
alarm. Weekday lists are persisted through a Room `TypeConverters` class. There are
no SQL files, migrations or cloud backend.

## Tech stack

- Kotlin 2.1.20, AGP 8.8.0, KSP 2.1.20-1.0.32.
- Jetpack Compose with Material 3, Compose BOM 2025.01.01, Navigation Compose 2.9.0.
- Room 2.7.1, WorkManager 2.10.0, Coroutines 1.10.1.
- compileSdk 35, minSdk 26, targetSdk 35, JVM target 17.
- Release builds enable R8 minification and resource shrinking.

## Tests and CI

Three test files exist:

- `AlarmSchedulerTest` (JVM unit test),
- `MathMissionTest` (JVM unit test),
- `AlarmListUITest` (Compose instrumented test).

`build.yml` runs on push and pull request to `main`: JDK 17, setup-gradle,
`gradle assembleDebug`, `gradle testDebugUnitTest`, `gradle lintDebug`, and a debug
APK artifact upload.

No build or test was executed for this documentation.

## Known limitations

- No Gradle wrapper is committed, so the documented `./gradlew` command does not
  work on a clean clone.
- Release signing depends on CI secrets and has not been exercised in this review.
- `WORK_MANAGER` is declared as a dependency, but its runtime use is not documented.
- Alarm reliability across OEM battery managers is unverified.

## License

MIT. See [`LICENSE`](LICENSE).
