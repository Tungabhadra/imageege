# Project Description

## Name
**imageege (Local Dream Android)**

## One-line summary
An Android-first, on-device Stable Diffusion app that runs image generation and editing locally using Qualcomm QNN (NPU) and MNN (CPU/OpenCL) backends.

## Problem it solves
Most image generation workflows depend on cloud GPUs and remote APIs. This project provides a local-first alternative so users can generate and edit images directly on supported Android devices, with offline-friendly execution and user-controlled model files.

## Target users
- Android users who want local AI image generation
- Developers experimenting with mobile diffusion inference
- Power users who need custom model import/conversion pipelines

## Core capabilities
- Local text-to-image and image-to-image generation
- Mask/inpaint support
- Downloadable built-in model catalog with resume and verification
- Custom model import (including custom NPU package workflows)
- Foreground/background service orchestration for stable long-running generation
- Native C++ inference server exposed through local HTTP endpoints for app integration

## Technical approach
- **UI + app logic:** Java/Kotlin Android app
- **Inference backend:** C++ runtime using QNN and MNN
- **Bridging pattern:** Android service layer launches and communicates with local native server (`/generate`, `/health`)

## Why it stands out
This repository combines practical Android UX workflows (projects/history/media/speech) with a production-style on-device inference backend, making it both an end-user app and a useful reference implementation for mobile generative AI.
