# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android instant messaging application called "Petter" built with Kotlin and Clean Architecture. The app features decentralized messaging using MQTT protocol, with support for direct messages, group chats, and user authentication. The project targets Android 10+ (minSdk 21) and uses Hilt for dependency injection.

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
- Android Gradle Plugin 7.4.2, Kotlin 1.8.20
- compileSdk 32, targetSdk 32, minSdk 21
- JVM target: 11
- Uses Hilt for dependency injection
- Room Database temporarily disabled due to Apple Silicon compatibility

## Architecture

### Clean Architecture Layers
The app follows Clean Architecture with clear separation of concerns:

- **Domain Layer**: Business logic, use cases, models, and repository interfaces
  - `domain/model/` - Core business models (User, Message, Group, Contact, etc.)
  - `domain/usecase/` - Use cases for specific business operations
  - `domain/repository/` - Repository interfaces for data access

- **Data Layer**: Data implementation and network handling
  - `data/remote/api/` - REST API service definitions
  - `data/remote/decentralized/` - MQTT-based decentralized messaging
  - `data/remote/network/` - Network configuration (Hilt modules)
  - `data/remote/model/` - Network DTOs and response models
  - `data/remote/repository/` - Remote data repository implementations
  - `data/repository/` - Base repository classes

- **Presentation Layer**: UI components and view models
  - `presentation/base/` - Base classes and common UI components
  - `presentation/ui/auth/` - Authentication (Login/Register)
  - `presentation/ui/chat/` - Direct messaging interface
  - `presentation/ui/group/` - Group chat functionality
  - `presentation/ui/main/` - Main navigation and home

### Key Technologies
- **MQTT Messaging**: Uses Eclipse Paho MQTT client for decentralized messaging
- **Hilt**: Dependency injection throughout the app
- **Coroutines**: Asynchronous programming with Kotlin coroutines
- **ViewBinding**: Type-safe view binding
- **Material Design**: Material Components for UI

### Message Flow Architecture
The app uses a decentralized messaging approach:
- Direct messages: `petter/msg/direct/{senderId}/{receiverId}`
- Group messages: `petter/msg/group/{groupId}`
- User status: `petter/status/online/{userId}`, `petter/status/offline/{userId}`

### Important Files
- `SimplifiedMqttManager.kt` - Core MQTT client handling decentralized messaging
- `MqttConfig.kt` - MQTT broker configuration
- `PetterApplication.kt` - Application class with Hilt setup
- `NetworkModule.kt` - Hilt module for network dependencies

## Development Notes

- Room Database is temporarily disabled due to Apple Silicon compatibility issues
- The app uses a simplified approach to group management via MQTT topics
- All network operations use coroutines for proper async handling
- Chinese comments are used throughout the codebase
- MQTT connection handles automatic reconnection and status broadcasting