# Bumblebee

[![Latest Release](https://img.shields.io/github/v/release/saisaran-m/bumblebee?color=9B7EDE&label=Latest%20Release)](https://github.com/saisaran-m/bumblebee/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK%20(v1.0.0)-F5C542?logo=android&logoColor=white)](https://github.com/saisaran-m/bumblebee/releases/download/v1.0.0/bumblebee.apk)
[![License: MIT](https://img.shields.io/badge/License-MIT-7C5CBF.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Offline](https://img.shields.io/badge/Network-100%25%20Offline%20P2P-black)](#key-features)

Bumblebee is an open-source, 100% offline peer-to-peer (P2P) communication app for Android. It enables smartphones to discover, connect, chat, and share photos directly without an active internet connection, cellular data, or external Wi-Fi routers.

Designed for off-grid scenarios such as natural disasters, remote hiking, flights, crowded events, or regions with poor network infrastructure, Bumblebee turns mobile devices into an ad-hoc local mesh network.

---

## Screenshots

| Chats | Direct P2P Chat | Emergency SOS | Settings & Profile |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/chats_screen.png" width="220" alt="Chats Screen" /> | <img src="docs/screenshots/chat_screen.png" width="220" alt="Direct Chat Screen" /> | <img src="docs/screenshots/buzz_sos_screen.png" width="220" alt="Emergency SOS Screen" /> | <img src="docs/screenshots/settings_screen.png" width="220" alt="Settings Screen" /> |

---

## Key Features

- **Zero-Internet Communication**: Connects directly device-to-device using Bluetooth Low Energy (BLE) for discovery and Wi-Fi Direct for high-bandwidth data transfer.
- **Push-To-Talk Voice Walkie-Talkie**: Record and transmit crisp offline voice memos with animated waveform visualization and playback controls.
- **High-Definition Photo Sharing**: Proprietary Nectar chunking engine streams crystal-clear 1280px HD images across radio packets with zero compression artifacts.
- **Direct Messaging**: One-on-one instant messaging with real-time delivery state tracking (`Sending`, `Sent`, `Delivered`, `Read`).
- **Live Radio Telemetry**: Tap connection status to inspect carrier protocol, mesh hop distance, packet throughput, and air-gap security metrics in real time.
- **Honeycomb Discovery Radar**: Real-time visual discovery of nearby devices with 4-digit mutual authentication codes for secure pairing.
- **Enhanced Emergency SOS Buzz**: Offline distress beacon that broadcasts urgent alerts with Google Maps GPS coordinates link and device battery level telemetry to all peers in range.
- **Privacy by Design**: No cloud servers, no account registration, no telemetry, and no centralized data collection. All chat history and media remain encrypted and stored strictly on the local device.

---

---

## Feature Status Matrix

| Feature | Status | Transport / Protocol | Verification Evidence |
| :--- | :---: | :--- | :--- |
| **Direct 1-to-1 P2P Messaging** | **Implemented** | Google Nearby Connections (`P2P_CLUSTER`) | Verified on 2 physical devices; bi-directional text with offline radio |
| **Delivery State Tracking** | **Implemented** | Application ACK Protocol (`NearbyPacket`) | Verified (`SENDING` ➔ `SENT` ➔ `DELIVERED` ➔ `READ` ➔ `FAILED`) |
| **Message Deduplication** | **Implemented** | Database ID Guard in `ChatRepository` | Verified; duplicate network frames discarded safely |
| **Push-To-Talk Voice Notes** | **Implemented** | AAC-LC (22.05 kHz / 32 kbps) Chunked | Verified audio recording, transmission, & waveform playback |
| **HD Photo Sharing (1280px)** | **Implemented** | Nectar Chunking Engine (18 KB slices) | Verified high-res image reconstruction with zero artifacts |
| **4-Digit Mutual PIN Verification** | **Implemented** | UKEY2 Short Authentication String (SAS) | Verified visual confirmation on both screens before pairing |
| **Emergency SOS Beacon** | **Implemented** | Nearby Broadcast + GPS & Battery Telemetry | Verified broadcast with Google Maps link & battery status |
| **Multi-Hop Relay Routing** | **Experimental (Roadmap)** | Multi-Hop Store-and-Forward | Protocol fields defined (`hopCount`, `maxHops`); 3-device relay in development |

---

## Technical Architecture & Transport Behavior

Bumblebee leverages **Google Play Services Nearby Connections API** with the **`P2P_CLUSTER`** strategy for physical radio communication:

- **Discovery & Negotiation**: Nearby Connections manages autonomous local discovery across Bluetooth Low Energy (BLE) and Bluetooth Classic.
- **Data Transport**: When high-bandwidth data (photos or voice notes) is transferred, Nearby Connections automatically negotiates high-speed Wi-Fi Direct or local Wi-Fi hotspots without requiring an external internet connection or router.
- **Transport Security**: Sessions are secured using UKEY2 elliptic-curve Diffie-Hellman key exchange, verified visually by both users via a matching 4-digit Short Authentication String (SAS).
- **Data at Rest**: Messages, media, and peer profiles are stored strictly on the local device within Android's private app sandbox (`/data/data/com.bumblebee.chat/databases/` via Room SQLite). Data is isolated by Android operating system Linux UID boundaries.
- **Emergency SOS Disclosure**: Triggering an Emergency SOS explicitly reads the device's last known GPS satellite coordinates and battery capacity percentage, broadcasting them in plaintext to all neighboring devices within radio range to facilitate search and rescue.

---

## Verified Devices & Test Results

| Device Pair | Android Version | Radio Hardware | Test Conditions | Outcome |
| :--- | :--- | :--- | :--- | :---: |
| **Google Pixel 7 ↔ Samsung Galaxy S22** | Android 14 ↔ Android 13 | Wi-Fi 6, BLE 5.2 | Air-Gapped (SIM removed, Wi-Fi router off) | Passed (100+ msgs, voice, HD photos) |
| **Samsung Galaxy S22 ↔ OnePlus Nord** | Android 13 ↔ Android 12 | Wi-Fi 5, BLE 5.0 | Air-Gapped (Airplane mode, BT/Wi-Fi on) | Passed (100+ msgs, voice notes) |
| **Redmi Note 11 ↔ Google Pixel 7** | Android 12 ↔ Android 14 | Wi-Fi 5, BLE 5.0 | High RF interference (Crowded room) | Passed (Direct P2P, SOS beacon) |

---


## Download & Installation

The pre-compiled, signed Android APK is available directly from the GitHub Releases page:

- **Direct Download**: **[Download bumblebee.apk (v1.0.0)](https://github.com/saisaran-m/bumblebee/releases/download/v1.0.0/bumblebee.apk)**
- **Releases Page**: [View All Releases & Changelogs](https://github.com/saisaran-m/bumblebee/releases)
- **Compatibility**: Android 8.0 (API level 26) or higher

---

## Offline Testing Guide

To test Bumblebee in a fully offline environment:

1. **Install the APK** on two physical Android devices running Android 8.0 (API 26) or higher.
2. **Disconnect from Internet**:
   - Turn **OFF** Mobile Data on both devices.
   - Disconnect from any local Wi-Fi router (the Wi-Fi toggle itself must remain **ON** so Android can operate the Wi-Fi Direct radio).
   - Ensure **Bluetooth** is turned **ON**.
3. **Launch the App**: Complete the initial profile setup (name and avatar).
4. **Grant Permissions**: Allow Bluetooth, Nearby Devices, and Location permissions (required by Android system APIs for P2P discovery).
5. **Connect**:
   - Navigate to the **Nearby (Honeycomb)** tab on both devices.
   - Tap **Scan** on one phone and verify the 4-digit code on the other.
   - Once connected, start exchanging messages and photos directly.

---

## Project Structure

```
app/src/main/java/com/bumblebee/chat/
├── BumblebeeApp.kt               # Application entry point & Hilt setup
├── MainActivity.kt                # Root activity & edge-to-edge host
├── core/
│   ├── constants/                 # App and protocol constants
│   ├── di/                        # Hilt dependency injection modules
│   ├── theme/                     # Material 3 colors, shapes, typography
│   └── utils/                     # DataStore and permission helpers
├── data/
│   ├── local/                     # Room database, entities, and DAOs
│   ├── nearby/                    # Google Nearby Connections manager & packet models
│   └── repository/                # Chat, peer, and preference repositories
├── domain/
│   └── model/                     # Core business entities (Message, Peer, Conversation)
└── ui/
    ├── buzz/                      # Emergency SOS beacon screen & ViewModel
    ├── chat/                      # Direct chat screen, message bubbles, photo viewer
    ├── chats/                     # Conversations list screen & ViewModel
    ├── components/                # Reusable Compose widgets (HexagonAvatar, dialogs)
    ├── navigation/                # Compose Navigation graph & routes
    ├── nearby/                    # Honeycomb radar discovery screen & ViewModel
    ├── onboarding/                # Initial setup screen & ViewModel
    └── settings/                  # App preferences, language, & reset session
```

---

## Building from Source

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 17 or higher
- Android SDK 35 (Compile SDK)
- Min SDK: Android 8.0 (API level 26)

### Build Commands

Clone the repository and compile using the Gradle wrapper:

```bash
git clone https://github.com/saisaran-m/bumblebee.git
cd bumblebee

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
