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