# SeniorMode (Android)

An Android application built with Gradle and the Android Gradle Plugin, To make smartphone experience easy for senior citizens. This README explains how to set up your development environment (Windows PowerShell), build and run the app, run tests, and troubleshoot common issues.

## Table of contents

- Summary
- Requirements
- Setup (step-by-step for Windows)
- Build, install & run (PowerShell)
- Testing
- Debugging & logs
- Troubleshooting
- Contributing & next steps

## Summary

The project contains a single Android app module located in `app/`. The Gradle wrapper (`gradlew`, `gradlew.bat`) is included so you can build without installing Gradle system-wide.

## Requirements

- Java Development Kit (JDK) 11 or newer. This project expects a Java 11+ runtime; use the JDK matching the project's Gradle/AGP settings.
- Android Studio (recommended) or command-line tools (sdkmanager, adb).
- Android SDK: `platform-tools`, one or more `platforms` (for your targetSdkVersion) and build-tools.
- A physical device or Android emulator to run the app.

## Setup (Windows PowerShell) — step by step

1) Install JDK

- Install a JDK (11+). AdoptOpenJDK / Temurin are good choices. Remember the installation path (example: `C:\Program Files\Java\temurin-11-jdk`).

Set `JAVA_HOME` (PowerShell, run as your user):

```powershell
setx JAVA_HOME "C:\Program Files\Java\temurin-11-jdk"
# To use the new value in current shell session:
$env:JAVA_HOME = "C:\Program Files\Java\temurin-11-jdk"
```

2) Install Android SDK (via Android Studio recommended)

- Open Android Studio and install the SDK, Android SDK Platform, and SDK Build-Tools for the API level used by the project (check `app/build.gradle` for `compileSdk` / `targetSdk`).
- Alternatively install command-line SDK tools and use `sdkmanager`:

```powershell
# Example: install platform-tools and API 33 build tools (adjust versions to project needs)
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"
```

Set `ANDROID_SDK_ROOT` (PowerShell):

```powershell
setx ANDROID_SDK_ROOT "C:\Users\<your-user>\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\<your-user>\AppData\Local\Android\Sdk"
```

3) Verify basic tooling

```powershell
java -version
adb --version
.\gradlew.bat -v
```

4) (Optional) Create an emulator

```powershell
# List system images
sdkmanager --list

# Create an AVD (example using avdmanager)
& "${env:ANDROID_SDK_ROOT}\cmdline-tools\latest\bin\avdmanager.bat" create avd -n SeniorModeAVD -k "system-images;android-33;google_apis;x86_64" --device "pixel"

# Start the emulator
& "${env:ANDROID_SDK_ROOT}\emulator\emulator.exe" -avd SeniorModeAVD
```

Note: paths and image names depend on installed SDK packages. Use Android Studio AVD Manager for an easier UI.

5) Open in Android Studio

- Start Android Studio, `File > Open`, pick the repository root. Let it sync Gradle and download dependencies.

## Build, install & run (PowerShell)

Open PowerShell in the project root (where `gradlew.bat` lives) and run:

```powershell
# Clean build outputs
.\gradlew.bat clean

# Build a debug APK
.\gradlew.bat assembleDebug

# Install to connected device/emulator
.\gradlew.bat installDebug

# Build an unsigned release APK
.\gradlew.bat assembleRelease
```

If you prefer Android Studio, use the Run button or create a Run configuration for the `app` module.

## Testing

Unit tests (JVM):

```powershell
.\gradlew.bat test
```

Instrumentation / UI tests (device or emulator required):

```powershell
.\gradlew.bat connectedAndroidTest
```

Tip: run a single test class with Gradle test filters (see project's Gradle configuration for test options).

## Debugging & logs

- View device logs (logcat):

```powershell
adb logcat --clear
adb logcat | Select-String "SeniorMode" -SimpleMatch
```

- Attach debugger in Android Studio or run the app in debug mode with the Gradle task `assembleDebug` and use `Run > Attach debugger to Android process`.

## Troubleshooting (common Windows issues)

- Long path / OneDrive: this project is inside OneDrive which can introduce path/permission issues. If you see errors related to file locks or long paths, try moving the project to a short path (e.g., `C:\Projects\SeniorMode`).
- Gradle/SDK missing: open Android Studio and install missing SDK components via the SDK Manager.
- Emulator not found: ensure `ANDROID_SDK_ROOT` is correct and `emulator` is installed; start emulator via AVD Manager.
- Device not detected: enable Developer Options and USB debugging on device and verify `adb devices` shows it.
- Out of memory on Gradle: set `org.gradle.jvmargs=-Xmx2g` in `gradle.properties` if needed.
- Permission issues installing APK: uninstall previous debug build or use `adb uninstall <package>` then `installDebug`.

## CI suggestions (optional)

If you'd like a GitHub Actions workflow, a minimal job should:

- Set up JDK 11
- Set up Android SDK (platform-tools, platforms and build-tools)
- Run `./gradlew test` and optionally `./gradlew assembleDebug`

I can add a ready-to-use `.github/workflows/android.yml` that runs on push/PR.

## Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feat/your-feature`
3. Implement changes and add tests
4. Run `./gradlew test` locally
5. Push branch and open a PR

If you want, I can add `CONTRIBUTING.md` and a GitHub Actions workflow to run tests automatically.

## License

No license file is included yet. If you want MIT or Apache-2.0, tell me and I will add an appropriate `LICENSE` file.

---

If you'd like I can also:

- Commit the updated `README.md` and push to `main` (I will not run git actions without your go-ahead),
- Add CI (GitHub Actions) that runs `./gradlew test` on push/PR, or
- Create `CONTRIBUTING.md` and `LICENSE` files.

Tell me which next step you want and I'll do it.
