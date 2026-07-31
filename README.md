# imageege (Local Dream Android)

`imageege` is an Android app for **on-device Stable Diffusion image generation and editing**.  
It combines a native C++ inference backend with an Android UI to run models locally using:

- **Qualcomm QNN (NPU path)** on supported Snapdragon devices
- **MNN CPU/OpenCL path** for broader compatibility

The app supports downloading ready-to-run model packs, running generation in background services, and managing local/custom models.

---

## What this project includes

- Android application module: `/home/runner/work/imageege/imageege/app`
- Native inference backend (C++): `/home/runner/work/imageege/imageege/app/src/main/cpp/src/main.cpp`
- Native build configuration (QNN + MNN + tokenizers): `/home/runner/work/imageege/imageege/app/src/main/cpp/CMakeLists.txt`
- Model conversion guide: `/home/runner/work/imageege/imageege/convert/README.md`

---

## Core features

- **Text-to-image and image-to-image** generation pipeline
- **Mask-based editing / inpaint workflow**
- **Model download manager** with resume support and file verification
- **CPU and NPU model catalogs** (including high-resolution patch downloads)
- **Custom model import** (including ZIP-based custom NPU model packages)
- **Embedding and LoRA-aware model conversion flow** (via conversion tooling)
- **Speech-to-prompt input**, image picking/camera input, recent edits, and project management UI
- **Foreground services** for backend lifecycle and long-running generation tasks

---

## High-level architecture

### Android layer

- Main app UI and user flows are implemented in Java/Kotlin under:
  - `/home/runner/work/imageege/imageege/app/src/main/java/io/github/xororz/localdream`
- Responsibilities:
  - prompt input and image selection
  - model management and downloads
  - generation orchestration via background/foreground services
  - result/history/project screens

### Native inference layer

- Main native entrypoint:
  - `/home/runner/work/imageege/imageege/app/src/main/cpp/src/main.cpp`
- Responsibilities:
  - CLI-based backend startup and model initialization
  - tokenization + prompt processing
  - diffusion steps and decoding
  - optional safety checker
  - local HTTP server (`/health`, `/generate`) used by Android services

### Service bridge

- Android starts native runtime through `BackendService`.
- Android sends generation requests through `BackgroundGenerationService` to local backend (`http://localhost:8081/generate`) and streams progress.

---

## Build requirements

- **Android Studio** (recommended)
- **JDK 17**
- **Android SDK 36** (compile/target)
- **Android NDK 27.0.12077973**
- Gradle wrapper included in repository

> Note: The native CMake file currently references a local `QNN_SDK_ROOT` path that must be set to your environment before successful NPU-native builds.

---

## Build and run (Android Studio)

1. Open `/home/runner/work/imageege/imageege` in Android Studio.
2. Let Gradle sync complete.
3. Ensure NDK and SDK versions above are installed.
4. Update QNN SDK path in:
   - `/home/runner/work/imageege/imageege/app/src/main/cpp/CMakeLists.txt`
5. Build and run the `app` module on an Android device.

---

## Models and conversion

- Built-in model metadata and download definitions are in:
  - `/home/runner/work/imageege/imageege/app/src/main/java/io/github/xororz/localdream/data/Model.kt`
- For external/custom conversion flow, follow:
  - `/home/runner/work/imageege/imageege/convert/README.md`

---

## Permissions and runtime behavior

The app requests camera/media/audio/network/foreground-service permissions for:

- image acquisition (camera/gallery)
- speech prompt capture
- model download and generation services

Manifest:
- `/home/runner/work/imageege/imageege/app/src/main/AndroidManifest.xml`

---

## Current status

This repository contains an actively integrated Android + native inference stack with both modernized UI flows and low-level backend integration. If you are onboarding, start by reviewing:

1. `/home/runner/work/imageege/imageege/app/src/main/java/io/github/xororz/localdream/MainActivity.java`
2. `/home/runner/work/imageege/imageege/app/src/main/java/io/github/xororz/localdream/data/Model.kt`
3. `/home/runner/work/imageege/imageege/app/src/main/cpp/src/main.cpp`

