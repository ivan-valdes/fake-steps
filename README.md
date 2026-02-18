<p align="center">
  <img src="https://raw.githubusercontent.com/ivan-valdes/fake-steps/master/app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" alt="FakeSteps icon"/>
</p>

<h1 align="center">FakeSteps</h1>

<p align="center">
  <strong>Schedule daily fake step entries to Health Connect</strong>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform: Android">
  <img src="https://img.shields.io/badge/API-28%2B-brightgreen.svg" alt="API 28+">
  <a href="https://buymeacoffee.com/PLACEHOLDER"><img src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?logo=buy-me-a-coffee&logoColor=black" alt="Buy Me a Coffee"></a>
</p>

---

FakeSteps is a simple Android app that writes fake step and walking session data to [Health Connect](https://health.google/health-connect/) on a daily schedule. Set your desired step count, walk duration, and preferred time -- FakeSteps handles the rest automatically.

## Screenshot

<!-- Replace with your actual screenshot -->
<p align="center">
  <img src="docs/screenshot.png" width="300" alt="FakeSteps screenshot"/>
</p>

## Features

- **Configurable step count** -- Set anywhere from 1 to 200,000 steps per day
- **Adjustable walk duration** -- 1 to 480 minutes per session
- **Scheduled execution** -- Pick a daily time with the built-in time picker
- **Reliable background execution** -- Uses `AlarmManager` exact alarms + `WorkManager` for reliability
- **Survives reboots** -- `BootReceiver` reschedules your alarm after device restart
- **Run Now button** -- Trigger an immediate step entry anytime
- **Battery optimization aware** -- Requests exemption to prevent the OS from killing scheduled work
- **Status dashboard** -- See Health Connect availability, permissions, battery, and scheduling state at a glance
- **Material You design** -- Dynamic colors on Android 12+ with Material3 components
- **Open source** -- MIT licensed, no tracking, no ads, no data collection

## Download

Grab the latest APK from the [Releases](https://github.com/ivan-valdes/fake-steps/releases/latest) page.

> **Requires:** Android 9 (API 28) or higher and [Health Connect](https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata) installed.

## Build from source

The project uses Docker so you don't need Android SDK installed on your machine.

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)

### Build

```bash
# Clone the repo
git clone https://github.com/ivan-valdes/fake-steps.git
cd fake-steps

# Build debug APK
docker compose run --rm builder ./gradlew assembleDebug

# Build release APK (requires signing config)
docker compose run --rm \
  -e KEYSTORE_PATH=/app/release.jks \
  -e KEYSTORE_PASSWORD=<your_password> \
  -e KEY_ALIAS=<your_alias> \
  -e KEY_PASSWORD=<your_key_password> \
  builder ./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk` (or `release/app-release.apk`).

### Helper script

```bash
./build-apk.sh debug    # or release
```

## How it works

1. You configure your desired step count, walk duration, and scheduled time
2. FakeSteps sets an exact alarm via `AlarmManager.setExactAndAllowWhileIdle()`
3. When the alarm fires, a `WorkManager` worker writes a `StepsRecord` and `ExerciseSessionRecord` (WALKING) to Health Connect
4. The alarm reschedules itself for the next day
5. If the device reboots, `BootReceiver` restores the schedule

## Permissions

FakeSteps requests the following Health Connect permissions:

- `WRITE_STEPS` -- to write step count records
- `WRITE_EXERCISE` -- to write walking exercise sessions

No data is read from Health Connect. No data is sent to any external server.

## Privacy

FakeSteps does not collect, store, or transmit any personal data. All step and exercise data is written locally to Health Connect on your device. See the full [Privacy Policy](https://ivan-valdes.github.io/fake-steps/privacy.html).

## Contributing

Contributions are welcome! Feel free to open issues and pull requests.

## Support

If you find FakeSteps useful, consider supporting the project:

<a href="https://buymeacoffee.com/PLACEHOLDER">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="50" alt="Buy Me a Coffee">
</a>

## License

This project is licensed under the MIT License -- see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made with care by <a href="https://github.com/ivan-valdes">ivan-valdes</a></p>
