# 🐝 Bumblebee

**Bumblebee** is a 100% offline peer-to-peer (P2P) mesh chat application for Android. It lets nearby devices discover, connect, and chat directly without internet, cell towers, or external Wi-Fi routers.

---

## ✨ Features

- **📶 100% Offline Mesh Communication**:
  - Operates entirely phone-to-phone via **Bluetooth Low Energy (BLE)** and high-speed **Wi-Fi Direct** radio.
  - Zero internet, zero data, and zero SIM card required.
- **💬 Direct Instant Messaging**:
  - Real-time 1-on-1 private messaging with delivery confirmations (`SENDING`, `SENT`, `DELIVERED`, `READ`).
- **📸 Offline Photo Sharing (Nectar)**:
  - Instant direct photo sharing compressed for real-time radio payloads with full-screen photo preview.
- **🕸️ Honeycomb Discovery Radar**:
  - Visual nearby device radar to discover and connect with neighboring devices in range.
  - 4-digit mutual authentication code to ensure trusted peer links.
- **🚨 Emergency SOS Buzz**:
  - High-priority offline distress beacon broadcasted to all nearby devices.
- **🎨 Lavender Honey Material 3 UI**:
  - Clean, modern, accessible design with custom hexagonal avatars, intuitive navigation, and fluid animations.
- **🔒 Privacy & Local Security**:
  - All chats and media stay strictly on your local device.
  - Session wipe and profile reset option in Settings ("Leave Hive & Reset Profile").

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.1.0
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture + Unidirectional Data Flow (UDF)
- **Dependency Injection**: Dagger Hilt
- **Local Persistence**: Room SQLite + DataStore Preferences
- **P2P Radios**: Google Play Services Nearby Connections (`P2P_CLUSTER`)
- **Image Loading**: Coil 2.7.0

---

## 🚀 How to Test Offline

1. Install the APK on two Android phones.
2. On **both** devices, turn **OFF** Mobile Data and disconnect from any home Wi-Fi router.
3. Keep the hardware **Bluetooth** and **Wi-Fi** switches turned **ON** in Quick Settings (Wi-Fi Direct uses the internal antenna for direct radio communication).
4. Launch Bumblebee on both phones.
5. In the **Honeycomb (Nearby)** tab, tap **Scan** on one phone and accept the connection on the other.
6. Start chatting and sharing photos offline!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — see the [LICENSE](LICENSE) file for details.
