# WebcamApp - Mobile Webcam Application

A robust Android application that repurposes old Android smartphones as webcams or security cameras. The application enables users to stream live video, record footage, detect motion, and view feeds remotely.

## Features

### Core Functionality
- **Dual Role System**: Choose between "Camera Device" and "Viewer Device" roles
- **Real-time Streaming**: Low-latency video and audio streaming using WebRTC
- **Motion Detection**: Intelligent motion detection with configurable sensitivity
- **Local Recording**: Continuous and motion-triggered video recording
- **Remote Control**: Control camera functions from viewer devices
- **Device Pairing**: Secure QR code-based device pairing system

### Camera Device Features
- Live video capture with configurable resolution and frame rate
- Audio capture and streaming
- Motion detection with zone-based detection
- Local video recording with circular storage management
- Power management and overheating protection
- Background operation and auto-start capability
- Flashlight control and camera switching

### Viewer Device Features
- Live feed viewing with multi-camera support
- Recorded video playback with timeline navigation
- Remote camera control (flashlight, camera switch, zoom)
- Two-way audio communication
- Device health monitoring
- Push notifications for motion events
- Cloud storage integration

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Android Architecture Components
- **Database**: Room Persistence Library
- **Dependency Injection**: Hilt
- **Networking**: WebRTC, OkHttp, Retrofit
- **Camera**: CameraX Jetpack Library
- **Cloud Services**: Firebase (Authentication, Firestore, Storage, FCM)
- **Concurrency**: Kotlin Coroutines
- **Data Storage**: DataStore Preferences

## Project Structure

```
app/src/main/java/com/webcamapp/mobile/
├── data/
│   ├── local/
│   │   ├── dao/           # Room DAOs
│   │   ├── entity/        # Room entities
│   │   ├── AppDatabase.kt # Room database
│   │   └── UserPreferences.kt # DataStore preferences
│   ├── model/             # Data models
│   └── repository/        # Repository layer
├── di/                    # Hilt dependency injection modules
├── service/               # Background services
├── receiver/              # Broadcast receivers
├── ui/
│   ├── screens/           # UI screens
│   │   ├── auth/          # Authentication screens
│   │   ├── camera/        # Camera device screens
│   │   ├── viewer/        # Viewer device screens
│   │   └── role/          # Role selection screens
│   └── theme/             # UI theme and styling
├── WebcamApplication.kt   # Application class
└── ui/MainActivity.kt     # Main activity
```

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0 Nougat)
- Kotlin 1.9.0+
- Google Play Services

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd WebcamApp
   ```

2. **Firebase Setup**
   - Create a new Firebase project
   - Enable Authentication (Email/Password, Google Sign-In)
   - Enable Cloud Firestore
   - Enable Firebase Storage
   - Enable Firebase Cloud Messaging
   - Download `google-services.json` and place it in the `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew build
   ```

### Configuration

1. **Firebase Configuration**
   - Update Firebase project settings in `google-services.json`
   - Configure Firestore security rules
   - Set up Firebase Storage rules

2. **WebRTC Signaling Server**
   - Deploy a signaling server for WebRTC peer connections
   - Update the signaling server URL in the app configuration

3. **Permissions**
   - The app will request necessary permissions at runtime
   - Ensure camera, microphone, and storage permissions are granted

## Development Phases

### Phase 1: Core Infrastructure & Authentication ✅
- [x] Project setup with Kotlin and Jetpack Compose
- [x] Firebase integration
- [x] User authentication and registration
- [x] Role selection UI
- [x] Basic user profile management

### Phase 2: Camera Device Core Functionality (In Progress)
- [ ] Camera preview and capture
- [ ] Audio capture
- [ ] Local video recording
- [ ] Motion detection
- [ ] Power management
- [ ] Background operation

### Phase 3: Viewer Device & Real-time Streaming (Planned)
- [ ] Device pairing system
- [ ] WebRTC signaling server
- [ ] Live video streaming
- [ ] Remote camera controls
- [ ] Push notifications

### Phase 4: Advanced Features (Planned)
- [ ] Recorded video playback
- [ ] Two-way audio
- [ ] Cloud storage integration
- [ ] Enhanced security features
- [ ] UI/UX refinements

### Phase 5: Testing & Deployment (Planned)
- [ ] Comprehensive testing
- [ ] Performance optimization
- [ ] Security auditing
- [ ] Release preparation

## Security Features

- Encrypted communication using TLS/SSL
- Secure authentication with Firebase
- Local data encryption
- Privacy zones for camera feeds
- Activity logging and monitoring
- Granular access control

## Performance Considerations

- Optimized for low battery consumption
- Efficient resource usage
- Adaptive streaming based on network conditions
- Background operation optimization
- Thermal management

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository or contact the development team.

## Key Features

### 4-Hour Recording Segmentation
- Each recording file is at most 4 hours long.
- If a recording session exceeds 4 hours, the app automatically starts a new file.
- This ensures files are manageable and easy to browse.

### User-Configurable Storage Limit
- Set a maximum storage allocation for recordings in **Advanced Settings**.
- Choose a specific size (in GB) or select **Unlimited**.
- If the limit is reached, the app automatically deletes the oldest recordings to make space for new ones.

### Automatic Old File Deletion
- When storage is full (based on your set limit), the system deletes the oldest files first.
- This ensures continuous operation without manual intervention.

### Recordings Grouped by Day
- The recordings list in the app UI is grouped by day (date).
- Easily browse and find recordings by the day they were made.

## Usage Instructions

### Setting the Storage Limit
1. Go to **Advanced Settings**.
2. In the **Storage Limit** section, enter your desired maximum storage in GB, or check **Unlimited Storage**.
3. The app will show your current usage and enforce the limit automatically.

### How 4-Hour Segmentation Works
- Start any recording (manual, scheduled, motion, or continuous).
- If the session lasts longer than 4 hours, the app will automatically close the current file and start a new one.
- This process is seamless and requires no user action.

### What Happens When Storage is Full?
- The app checks your storage usage after every new recording.
- If the limit is reached, the oldest files are deleted until there is enough space for new recordings.
- If **Unlimited** is selected, no files are deleted automatically.

### Browsing Recordings by Day
- Open the **Recordings** section in the app.
- Recordings are grouped under headers for each day (e.g., 2024-06-10).
- Each entry shows the time, duration, and file size.

## FAQ

**Q: Can I change the 4-hour segmentation interval?**
A: Not currently, but this can be made configurable if needed.

**Q: What happens if the app can't delete old files (e.g., permission issues)?**
A: The app will log an error and may stop new recordings until space is available.

**Q: How do I see how much space is used?**
A: The current usage is shown in **Advanced Settings** under **Storage Limit**.

**Q: Are recordings deleted if I select Unlimited and run out of device space?**
A: No, the app will not delete files in Unlimited mode. You are responsible for managing device storage in this case.

---

For more details, see the in-app help or contact support.

# WebcamApp

## Key Features

### Modern UI/UX (Material You)
- **Material You dynamic theming**: Adapts to your device’s wallpaper and system theme.
- **Bottom navigation bar**: Quick access to Home (Live), Recordings, Devices, and Settings.
- **Home/Live screen**: Edge-to-edge video, overlay controls (HD, record, mute, fullscreen), device selector, and recent recordings.
- **Recordings screen**: Grouped by day with sticky headers, thumbnails, search/filter, swipe to delete, and quick play.
- **Devices screen**: Device cards with status, battery, last seen, and quick actions.
- **Settings screen**: Sectioned, icons, sliders, dropdowns, and inline values.
- **Interactive video player**: Floating, auto-hide controls, animated seek bar, quality/speed menus, and modern overlays.
- **Smooth transitions and animations**: For navigation, controls, and overlays.
- **Accessibility**: Large touch targets, high-contrast mode, content descriptions.

## Usage Instructions

- Use the bottom navigation bar to switch between Home, Recordings, Devices, and Settings.
- On the Home screen, select a device, view live video, and use overlay controls.
- On the Recordings screen, browse by day, search/filter, swipe to delete, and tap to play.
- On the Devices screen, manage your paired devices.
- On the Settings screen, customize storage, overlays, and more.

## Coming Soon
- Picture-in-picture support for video player.
- Animated transitions for device/recording cards.
- More advanced search and filtering for recordings.
- Customizable overlays (app name, logo, etc.).
- Cloud/remote access features (optional backend).

---

For more details, see the in-app help or contact support.

## Automatic Low Power Streaming
- When the camera device battery drops below 5% or the device enters low power mode, the app automatically switches to low quality video streaming (lower resolution and frame rate) to save battery and data.
- When the battery returns to normal or exits low power mode, the camera automatically restores the previous (higher) quality.
- This process is seamless and requires no user action.
- You can customize the quality settings in Advanced Settings.