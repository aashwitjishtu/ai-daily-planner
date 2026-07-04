# 🌌 AI Daily Planner — Sleek Interface Edition

An elegant, modern, AI-integrated productivity, scheduling, and habit-tracking Android application designed to elevate daily focus. Built entirely with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **Google's Gemini API** via the Firebase AI SDK, it offers a seamless, offline-first personal workspace wrapped in a high-contrast premium theme.

---

## 🎨 Premium Theme: "Sleek Interface"

The application is styled with a highly customized, eye-safe **"Sleek Interface"** design language that avoids generic templates in favor of a modern, structured visual identity:

- **Sleek Light & Dark Palettes**: Handcrafted color schemes featuring **Sleek Light Slate** (`#F8F9FF`) and **Sleek Dark Slate** (`#0B0F19`) backgrounds.
- **Vibrant Accent Colors**: Bold interactive highlights including **Accent Indigo** (`#4F46E5`), **Teal** (`#14B8A6`), **Amber** (`#FB923C`), and **Rose** (`#F43F5E`).
- **Dynamic Visual Hierarchy**:
  - Soft-shadow rounded cards (`20.dp` to `24.dp` corners) with clean borders (`1.dp` thickness).
  - Elegant linear gradient cards (Indigo to Purple) for AI interaction banners.
  - Flush vertical color-indicator bars on task items mapping high, medium, and low priority values.
  - Dynamic font weights and contrast states to emphasize active navigation tabs, action triggers, and completed states.

---

## 🚀 Key Features

### 1. Interactive AI Productivity Dashboard
*   **Gemini Day Plan suggestions**: An interactive AI suggestion card powered by Gemini that dynamically organizes your focus windows for maximum efficiency.
*   **Daily Productivity Meter**: Stylish metric indicators displaying completion percentages and trend comparisons against the previous day.
*   **Sleek Today's Schedule**: A quick-scan calendar-bound checklist with customized priority tags and task markers.

### 2. Comprehensive Task & Schedule Planner
*   **Fully-Featured Task Management**: Easily create, view, complete, or delete daily goals.
*   **Priority Matrix**: Assign priorities (High, Medium, Low) that color-code individual task cards with side-border indicator strips.
*   **Fluid Transitions**: Elegant spacing, generous layout padding, and Material Design ripples providing responsive micro-interactions.

### 3. Smart AI Chatbot Assistant
*   **Interactive Chat Companion**: Chat directly with a customized Gemini model tailored to provide organization hacks, scheduling recommendations, and motivational routines.
*   **Polished Dialogs**: Custom chat bubbles, animated typing indicators, and distinctive user/bot avatar indicators.

### 4. Habit Builder & Streak Tracker
*   **Routine Trackers**: Build positive recurring actions and check off daily streaks.
*   **Milestone Rewards**: Track consecutive streak days through visual completion elements.

### 5. Calendar & Analytics Views
*   **Agenda Mapping**: Visually trace upcoming deadlines on a clean timeline calendar.
*   **Productivity Reports**: High-level statistical charts plotting finished vs. pending schedules.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin (100%)
- **UI Architecture**: Jetpack Compose, Material Design 3, MVVM (Model-View-ViewModel) pattern
- **Local Database**: Room SQLite ORM with Kotlin Symbol Processing (KSP) and asynchronous Flow/Coroutines
- **Artificial Intelligence**: Firebase AI SDK / Google Gemini API (REST client with secure API Key routing)
- **Navigation**: Jetpack Navigation Compose with type-safe routing
- **Networking & Parsing**: Retrofit, OkHttp 4, Moshi (Kotlin JSON codegen)
- **Image Loading**: Coil Compose
- **Testing Engine**: Robolectric (JVM-based UI and behavior testing) & Roborazzi (Visual snapshot regression checking)

---

## 📦 Installation & Setup

Follow these instructions to run the project locally or compile a test build:

### Prerequisites
*   **Android Studio** Ladybug (2024.2.1) or newer
*   **JDK 17** configured in your system and Android Studio path
*   **Android SDK 34+** installed

### Step 1: Clone the Repository
```bash
git clone <repository_url>
cd ai-daily-planner
```

### Step 2: Configure Secrets & API Keys
This project uses the **Secrets Gradle Plugin** to load API keys securely without hardcoding them in the source.

1. Create a `.env` file in the root directory:
   ```env
   # Add your Google Gemini API Key here
   GEMINI_API_KEY="AIzaSyYourGeminiApiKeyHere"
   ```
2. Build files automatically ingest this key and supply it via `BuildConfig.GEMINI_API_KEY`.

### Step 3: Build & Install
To compile and assemble the application using Gradle, execute the following commands in your terminal:

*   **Compile the Applet**:
    ```bash
    gradle assembleDebug
    ```
*   **Run Local Unit & Integration Tests**:
    ```bash
    gradle :app:testDebugUnitTest
    ```
*   **Verify Roborazzi Screenshot Tests**:
    ```bash
    gradle :app:verifyRoborazziDebug
    ```

---

## 📁 Project Structure

```
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/          # Source Code
│   │   │   │   ├── MainActivity.kt        # Application Entry Point & Navigation
│   │   │   │   ├── data/                  # Room Entities, DAOs, and Repositories
│   │   │   │   └── ui/
│   │   │   │       ├── screens/           # Modular Compose UI Screen Layouts
│   │   │   │       └── theme/             # Custom "Sleek Interface" Design Palette
│   │   │   └── res/                       # Standard Android Resource files
│   └── build.gradle.kts                   # Module level dependencies
├── build.gradle.kts                       # Project level configurations
├── settings.gradle.kts                    # Gradle Plugin and Module repositories
├── .env.example                           # Template for secure environment variables
└── README.md                              # This documentation file
```
