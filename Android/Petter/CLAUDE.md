# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "Petter" built with Kotlin and Android Gradle Plugin 7.2.1. The project targets Android 10+ (minSdk 29) and uses a standard Android app structure.

## Build and Development Commands

### Building the App
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Build and install debug version to connected device/emulator
./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "hi.petter.ExampleUnitTest"
```

### Gradle Configuration
The project uses Chinese mirror repositories (Aliyun) for better build performance in China. Key settings:
- Android Gradle Plugin 7.2.1
- Kotlin 1.7.10
- compileSdk 32, targetSdk 32
- JVM target: 1.8

## Architecture

### Project Structure
- `app/src/main/java/hi/petter/` - Main application source code
- `app/src/test/` - Unit tests (run on JVM)
- `app/src/androidTest/` - Instrumented tests (run on Android device)
- `app/src/main/res/` - Android resources (layouts, values, etc.)

### Key Components
- **MainActivity** - Single activity app entry point
- **Application ID**: `hi.petter`
- **Package Structure**: Standard Android package hierarchy under `hi.petter`

### Dependencies
- AndroidX libraries (core, appcompat, material, constraintlayout)
- JUnit for unit testing
- Espresso for UI testing
- Material Design components

## Development Notes

- The project uses Kotlin as the primary development language
- Follows standard Android architecture patterns
- No additional frameworks or libraries beyond basic AndroidX dependencies
- Uses official Material Design components
- Chinese repository configuration is in place for Gradle dependencies