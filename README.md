# CloudCast - Smart Weather & History App

CloudCast is a modern Android application that provides real-time weather information based on the user's GPS location. It features a personalized experience with local authentication and persistent weather history tracking.

## 🚀 Key Features

### 📍 GPS & Real-time Location Integration
* **Automatic Detection**: Uses Android's `LocationManager` to fetch high-accuracy coordinates (Latitude and Longitude).
* **Smart Fetching**: Dynamically queries weather data immediately after the location is resolved.

### 🔐 Local Authentication & User Management
* **Personalized Accounts**: Supports User Registration with Username, Email, and Password.
* **Session Persistence**: Built with `SharedPreferences` to keep users logged in across app restarts.
* **Input Validation**: Robust checks for empty fields and password matching to ensure data integrity.

### 💾 Storage & History Tracking
* **User-Specific History**: Weather history is tied to individual accounts. Each user has their own private history log.
* **JSON Serialization**: Utilizes the `Gson` library for efficient local storage of complex weather records.
* **Automated Cleanup**: Keeps the 20 most recent searches to optimize device storage and performance.

### ☁️ Weather API Integration (Powered by WeatherAPI.com)
* **Live Weather Metrics**: Displays Temperature, "Feels Like" conditions, Humidity, and Wind Speed.
* **Precipitation Data**: Tracks real-time rainfall/precipitation in millimeters (mm).
* **4-Day Forecast**: Provides a sleek, edge-to-edge forecast row for upcoming days.
* **Historical Lookup**: Includes an integrated Calendar view to retrieve weather data for past dates.

### 🎨 Modern UI/UX
* **Immersive Design**: Full-screen experience achieved by removing the standard ActionBar via custom themes.
* **Adaptive Icons**: Optimized app launcher icon with proper safe-zone insets for a professional look on all device menus.
* **Clean Layouts**: Organized data rows and Material Design cards for a premium feel.

## 🛠️ Technology Stack
* **Language**: Java
* **Network**: HttpURLConnection & DoH (DNS over HTTPS) for reliable API connectivity.
* **Parsing**: Google Gson for JSON processing.
* **UI**: Material Components for Android.
* **Persistence**: SharedPreferences.

---
*Developed for a final project presentation.*
