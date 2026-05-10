# Bubble Shooter Classic Pop

Native Android Bubble Shooter MVP built with Kotlin and Jetpack Compose.

## Project Details

- Package: `com.brian.bubbleshooterclassicpop`
- Minimum SDK: 26
- Compile SDK: Android 36.1
- Target SDK: 36
- UI: Jetpack Compose with Compose Canvas for the game board
- Gameplay: offline-only local state, no ads, no purchases, no backend

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select `D:\Project\BubbleShooterClassicPop`.
3. Let Gradle sync.
4. If your SDK is not at `D:\android\SDK`, update `local.properties` so `sdk.dir` points to your Android SDK.

## Build

From PowerShell in `D:\Project\BubbleShooterClassicPop`:

```powershell
$env:JAVA_HOME="D:\android\Android Studio\jbr"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat testDebugUnitTest assembleDebug
```

The Gradle wrapper is checked in. The project was verified with Android Studio JBR at `D:\android\Android Studio\jbr` and Android SDK at `D:\android\SDK`.

## Run

Use Android Studio's Run button with the `app` configuration, or install the debug APK from:

```text
app\build\outputs\apk\debug\app-debug.apk
```

To install from PowerShell:

```powershell
D:\android\SDK\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
D:\android\SDK\platform-tools\adb.exe shell am start -n com.brian.bubbleshooterclassicpop/.MainActivity
```

## Verification Status

- `testDebugUnitTest` covers grid conversion, wall/collision logic, match detection, floating removal, score updates, win detection, lose detection, and pause/shoot behavior.
- `assembleDebug` builds the installable debug APK.

## Current MVP Limitations

- Local/offline single-player only.
- Simple deterministic bubble physics, not a full physics engine.
- No music, sound effects, ads, purchases, cloud save, or analytics.
- Levels scale by initial row count and move count only.
- Touch controls support tap-to-shoot and drag-release shooting.

## Future Improvement Ideas

- Add generated pop, bounce, win, and lose sound effects.
- Add animated bubble popping and falling.
- Add handcrafted level layouts and level goals.
- Add color-blind friendly bubble markings.
- Add high-score persistence using local storage.
- Add tablet-specific layout polish and accessibility settings.
