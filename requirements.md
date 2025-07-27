Mobile Webcam Application: Detailed Requirements Document
1. Introduction
1.1 Purpose
This document outlines the detailed functional and non-functional requirements for a mobile application designed to repurpose old Android smartphones as webcams or security cameras. The application will enable users to stream live video, record footage, detect motion, and view feeds remotely, offering a cost-effective surveillance or communication solution.
1.2 Scope
The application will consist of two primary modes: "Camera Device" and "Viewer Device." A user will log in and choose their role.
Camera Device: An Android phone acting as the video source, responsible for capturing, storing, and streaming video/audio, and performing motion detection.
Viewer Device: An Android phone used to remotely monitor, control, and access recorded footage from one or more Camera Devices.
This document focuses on the core functionalities identified, along with enhancements based on common user feedback and security camera app requirements.
1.3 Target Audience
Individuals looking for an affordable home security solution.
Parents wanting to monitor children or pets.
Small business owners needing basic surveillance.
Users who want to repurpose old, unused Android smartphones.
2. User Roles
2.1 Camera Device User
A user who configures an old Android phone to act as a video camera. This device will capture video and audio, perform motion detection, and store/stream data.
2.2 Viewer Device User
A user who uses another Android phone to view live feeds, access recorded footage, and manage settings of one or more Camera Devices.
3. Functional Requirements
3.1 Authentication and Account Management
FR1.1 User Registration: Users shall be able to register for an account using email/password or Google account.
FR1.2 User Login: Users shall be able to log in to their account.
FR1.3 Role Selection: After successful login, the user shall be prompted to select their role: "Camera" or "Viewer."
FR1.4 Device Pairing:
Camera Devices shall generate a unique pairing code (e.g., QR code or alphanumeric string).
Viewer Devices shall be able to scan the QR code or enter the alphanumeric string to pair with a Camera Device.
A single Viewer Device shall be able to pair with multiple Camera Devices.
A single Camera Device shall be able to be viewed by multiple Viewer Devices (shared access).
FR1.5 Password Reset: Users shall be able to reset their password.
FR1.6 User Profile Management: Users shall be able to update their profile information (e.g., email, password).
3.2 Camera Device Functionality
FR2.1 Live Video Feed Capture: The Camera Device shall capture live video from its selected camera (front/rear).
FR2.1.1 Camera Selection: User shall be able to select between front and rear cameras.
FR2.1.2 Resolution & Frame Rate Settings: User shall be able to adjust video resolution (e.g., 360p, 480p, 720p, 1080p) and frame rate (e.g., 15fps, 30fps).
FR2.1.3 Flashlight Control: User shall be able to toggle the device's flashlight on/off remotely (from Viewer).
FR2.2 Audio Capture: The Camera Device shall capture audio from its microphone.
FR2.3 Local Video Recording: The Camera Device shall record video locally to its internal storage.
FR2.3.1 Continuous Recording: Option to record continuously.
FR2.3.2 Motion-Triggered Recording: Automatically record when motion is detected.
FR2.3.3 Recording Duration: User shall be able to set the duration of motion-triggered recordings (e.g., 10s, 30s, 60s).
FR2.4 Motion Detection: The Camera Device shall implement motion detection.
FR2.4.1 Sensitivity Adjustment: User shall be able to adjust motion detection sensitivity.
FR2.4.2 Zone-Based Detection: User shall be able to define specific areas within the camera's view for motion detection.
FR2.4.3 Motion Log: Maintain a log of detected motion events with timestamps.
FR2.5 Power Management:
FR2.5.1 Battery Level Display: Display the current battery level of the Camera Device.
FR2.5.2 Overheating Alert: Notify the user (on both Camera and Viewer) if the device is overheating.
FR2.5.3 Scheduled Operation: Allow setting specific times for the camera to start and stop recording/streaming.
FR2.5.4 Screen Dimming/Off: Option to dim or turn off the Camera Device's screen while operating.
FR2.6 Auto-Start: The Camera Device application shall automatically launch and resume its camera role upon device reboot (if configured to do so).
FR2.7 Background Operation: The Camera Device application shall be able to run in the background, allowing other apps to be used (with potential performance/battery impact).
3.3 Viewer Device Functionality
FR3.1 Live Feed Viewing: The Viewer Device shall display live video and audio feeds from paired Camera Devices.
FR3.1.1 Multi-Camera View: User shall be able to view multiple live feeds simultaneously (e.g., grid view).
FR3.1.2 Full-Screen View: User shall be able to switch to full-screen view for a single camera.
FR3.2 Recorded Video Playback: The Viewer Device shall allow playback of recorded videos stored on the Camera Device.
FR3.2.1 Timeline Navigation: A visual timeline for easy navigation through recorded footage.
FR3.2.2 Jump to Motion Events: Ability to quickly jump to specific motion-detected events on the timeline.
FR3.2.3 Playback Speed Control: Options to play recorded video at different speeds (e.g., 0.5x, 1x, 2x).
FR3.2.4 Download Clips: User shall be able to download recorded video clips to the Viewer Device.
FR3.3 Notifications: The Viewer Device shall receive real-time push notifications for motion detection events from paired Camera Devices.
FR3.3.1 Customizable Notification Settings: User shall be able to enable/disable notifications and set quiet hours.
FR3.4 Remote Camera Control: The Viewer Device shall provide controls to manage the paired Camera Device.
FR3.4.1 Start/Stop Recording: Remotely initiate or stop recording on the Camera Device.
FR3.4.2 Toggle Flashlight: Remotely turn the Camera Device's flashlight on/off.
FR3.4.3 Switch Camera: Remotely switch between front/rear cameras on the Camera Device.
FR3.4.4 Digital Zoom: Apply digital zoom to the live feed.
FR3.4.5 Take Snapshot: Capture a still image from the live feed.
FR3.5 Device Health Monitoring: Display the status of paired Camera Devices on the Viewer.
FR3.5.1 Battery Level: Show current battery percentage.
FR3.5.2 Storage Remaining: Show available storage space.
FR3.5.3 Connection Status: Indicate online/offline status and connection quality.
FR3.6 Two-Way Audio:
FR3.6.1 Push-to-Talk: Viewer can speak through the Camera Device's speaker.
FR3.6.2 Listen-In: Viewer can hear audio from the Camera Device's microphone.
FR3.6.3 Volume Control: Adjustable volume for both incoming and outgoing audio.
3.4 Video Streaming and Recording
FR4.1 Real-time Streaming: The application shall provide low-latency, real-time video and audio streaming between Camera and Viewer devices.
FR4.2 Adaptive Streaming: The video stream shall adapt to network conditions, adjusting resolution/bitrate to maintain connection stability.
FR4.3 Local Storage Management:
FR4.3.1 Storage Capacity Display: Show remaining local storage on the Camera Device.
FR4.3.2 Circular Recording: Automatically delete oldest recordings when storage is full (user configurable).
FR4.4 Cloud Storage Integration (Optional/Premium):
FR4.4.1 Integration with Google Drive/Dropbox: Option to upload motion-triggered clips or continuous recordings to a linked cloud storage account.
FR4.4.2 Cloud Storage Management: View and manage cloud-stored recordings from the Viewer Device.
3.5 User Interface and Experience (UI/UX)
FR5.1 Intuitive Design: The UI shall be clean, intuitive, and easy to navigate for both Camera and Viewer modes.
FR5.2 Responsive Layout: The UI shall adapt gracefully to different screen sizes and orientations (portrait/landscape) on Android devices.
FR5.3 Clear Status Indicators: Provide clear visual indicators for connection status, recording status, motion detection, battery level, etc.
FR5.4 Accessibility: Consider basic accessibility features (e.g., sufficient contrast, legible fonts).
3.6 Security
FR6.1 Secure Authentication: Implement strong authentication mechanisms (e.g., token-based authentication, secure password hashing).
FR6.2 Encrypted Communication: All video, audio, and control data transmitted between devices shall be encrypted (e.g., using TLS/SSL).
FR6.3 Data Privacy: User data and video footage shall be protected from unauthorized access.
FR6.4 Privacy Zones: Allow users to define areas in the camera feed that are permanently masked or blurred for privacy.
FR6.5 Activity Log: Maintain a log of login attempts, device pairing, and remote control actions.
3.7 Error Handling
FR7.1 Network Error Handling: Gracefully handle network disconnections, re-establish connections, and inform the user.
FR7.2 Storage Full Handling: Notify the user when local storage is full and suggest actions (e.g., enable circular recording, clear old footage).
FR7.3 Camera/Microphone Access Issues: Inform the user if camera or microphone permissions are missing or if the hardware is unavailable.
FR7.4 Overheating Protection: Implement automatic pausing or shutdown of camera functions if the device overheats dangerously.
4. Non-Functional Requirements
4.1 Performance
NFR1.1 Low Latency: Live video streaming latency should be minimal (ideally under 500ms) for a smooth viewing experience.
NFR1.2 Efficient Resource Usage: The Camera Device app should be optimized for low CPU and RAM usage to prevent overheating and conserve battery.
NFR1.3 Fast Loading Times: The application should launch quickly, and video feeds should start streaming promptly.
4.2 Security
NFR2.1 Data Encryption: All data in transit and at rest (especially recordings) must be encrypted using industry-standard protocols (e.g., AES-256 for storage, TLS 1.2+ for transport).
NFR2.2 Authentication Robustness: The authentication system must be resistant to common attacks (e.g., brute-force, credential stuffing).
NFR2.3 Access Control: Implement granular access control to ensure only authorized Viewer Devices can access specific Camera Devices.
4.3 Usability
NFR3.1 Ease of Setup: The initial setup and pairing process for both Camera and Viewer devices should be straightforward and require minimal steps.
NFR3.2 Intuitive Navigation: Users should be able to easily find and use all features without extensive training.
NFR3.3 Clear Feedback: The application should provide clear and timely feedback to user actions and system status.
4.4 Reliability
NFR4.1 Stability: The application should operate stably without frequent crashes or freezes, especially the Camera Device in continuous operation.
NFR4.2 Connection Resilience: The application should be resilient to temporary network interruptions and automatically attempt to reconnect.
NFR4.3 Data Integrity: Recorded video and audio data should be stored without corruption.
4.5 Scalability
NFR5.1 Multiple Devices: The system should support a single Viewer managing multiple Camera Devices, and a single Camera Device being viewed by multiple Viewers.
NFR5.2 User Base: The backend infrastructure should be scalable to support a growing number of users and concurrent streams.
4.6 Maintainability
NFR6.1 Modular Codebase: The application code should be modular, well-documented, and easy to understand for future enhancements and bug fixes.
NFR6.2 Logging: Implement comprehensive logging for debugging and performance monitoring.
4.7 Compatibility
NFR7.1 Android Versions: The application should support a reasonable range of Android OS versions (e.g., Android 7.0 (Nougat) and above) to accommodate older phones.
NFR7.2 Device Hardware: The application should function correctly across various Android phone models with different camera hardware.
4.8 Battery Life/Power Management
NFR8.1 Optimized Power Consumption: The Camera Device application should be highly optimized to minimize battery drain during continuous operation, especially when plugged in.
NFR8.2 Thermal Management: The application should actively monitor device temperature and implement mechanisms to prevent overheating during prolonged use.
5. Technical Considerations (High-Level)
Platform: Native Android (Java/Kotlin) for both Camera and Viewer applications.
Networking:
Peer-to-peer (P2P) connection where possible for direct streaming.
Relay server/STUN/TURN for NAT traversal and firewall bypassing.
WebSockets or similar for real-time signaling and control commands.
Video Encoding/Streaming Protocols: H.264/H.265 for video, AAC for audio. RTP/RTCP or WebRTC for streaming.
Database/Storage:
Local storage on Camera Device for recordings.
Firebase Firestore for user authentication, device pairing information, and potentially motion event logs.
Cloud storage APIs (Google Drive API, Dropbox API) for optional cloud backup.
Cloud Services: Firebase Authentication, Firebase Cloud Messaging (FCM) for push notifications, potentially a custom backend for signaling/relay.
This document provides a comprehensive overview of the requirements. Further detailed design and technical specifications will be developed based on these requirements.
