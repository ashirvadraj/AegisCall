# ??? AegisCall ? Next-Gen Ad-Free Truecaller Alternative

**AegisCall** is an open-source, 100% advertisement-free, and privacy-preserving Android application built with modern Android architecture (Jetpack Compose, Material 3, Room, Kotlin Coroutines).

---

## ?? Key Highlights & Parity with Truecaller

- **Real-Time Caller ID Heads-Up Overlay**: Real-time identification overlay card during incoming/outgoing calls displaying caller name, city/state, carrier, category, and dynamic spam risk rating.
- **Native Android 10+ Call Screening**: Integrates with `android.telecom.CallScreeningService` to silently reject and drop robocalls, telemarketers, and fraudsters without the screen lighting up.
- **Smart SMS & OTP Auto-Copy**: Automatically categorizes messages into *Personal*, *Transactions*, *OTPs*, *Promotions*, and *Spam Quarantine*. Features a one-tap notification button to copy OTP verification codes to clipboard.
- **T9 Smart Dialer**: Fast numeric keypad with instant caller search, speed dial, and direct call trigger.
- **Enriched Call Logs**: Detailed history logs with call direction badges, duration, timestamps, and notes.
- **Zero Ads & Zero Telemetry**: Absolutely no Google AdMob, no video ads, no analytics SDKs, and no tracking.

---

## ?? Exclusive Features Beyond Truecaller

1. **Zero-Knowledge Privacy / Anti-Harvesting**: Truecaller uploads users' entire phonebooks to remote servers. AegisCall operates strictly on-device using local SQLite/Room caching and offline heuristic signatures. Your contacts are never uploaded.
2. **On-Device AI & Phishing Defense Engine**: NLP pattern recognition for banking scams, fake lottery alerts, IRS threats, and obfuscated/shortened phishing URLs.
3. **Direct WhatsApp Launcher**: Chat directly with unknown callers without saving them to your address book.
4. **Emergency Escape Fake Call**: Built-in simulated incoming call generator with custom caller identity, scheduled timer (15s, 30s, 1m, 5m), and realistic ring simulation to gracefully exit meetings or uncomfortable situations.
5. **Encrypted Call Notes & Reminders**: Attach private notes directly to phone numbers.
6. **Encrypted Backup & Export**: Export blocklists, call logs, and notes to encrypted JSON or CSV.
7. **AMOLED Dark Mode & Material You**: True black background optimized for battery conservation and adaptive Material 3 dynamic colors.

---

## ??? Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Edge-to-Edge
- **Architecture**: MVVM, Clean Architecture, Repository Pattern
- **Persistence**: Room Database (SQLite), Flow & Coroutines
- **Telecom**: `CallScreeningService`, `InCallService`, `WindowManager` Overlay
- **Target SDK**: Android 14 / 15 (API 34 / 35), Min SDK 26 (Android 8.0 Oreo)

---

## ?? Download Prebuilt APK

The compiled debug APK is available in the [Releases](https://github.com/ashirvadraj/AegisCall/releases) tab.
