# WebcamApp Phase 2 Testing Guide

## 🧪 **Testing Overview**

This guide provides comprehensive testing instructions for **Phase 2: Camera Device Core Functionality** of the WebcamApp. The application has been successfully implemented with all core camera features, motion detection, recording, and power management.

## 📱 **Prerequisites for Testing**

### **Required Setup:**
1. **Android Device or Emulator** (API 24+ recommended)
2. **Android Studio** (latest version)
3. **Firebase Project** (for notifications)
4. **Camera Permissions** (granted on device)

### **Development Environment:**
- **Java 11+** or **OpenJDK 11+**
- **Android SDK** (API 34 recommended)
- **Gradle 8.2+**

## 🚀 **Build and Installation**

### **1. Setup Android Environment**
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/your/android/sdk

# Add platform-tools to PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### **2. Configure Firebase**
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package name: `com.webcamapp.mobile`
3. Download `google-services.json` and place it in `app/` directory
4. Enable Firebase Cloud Messaging in Firebase console

### **3. Build the Application**
```bash
# Clean and build
./gradlew clean
./gradlew build

# Install on connected device
./gradlew installDebug
```

## 🎯 **Feature Testing Checklist**

### **✅ Authentication & Role Selection**
- [ ] **Login Screen**: Test email/password authentication
- [ ] **Register Screen**: Test new user registration
- [ ] **Role Selection**: Verify Camera Device vs Viewer Device selection
- [ ] **Navigation**: Ensure proper flow between screens

### **✅ Camera Functionality**
- [ ] **Camera Preview**: Live camera feed displays correctly
- [ ] **Camera Switching**: Front/rear camera toggle works
- [ ] **Flash Control**: Torch on/off functionality
- [ ] **Resolution Settings**: Different resolution options work
- [ ] **Camera State**: Proper state management (IDLE, STARTING, READY, ERROR)

### **✅ Video Recording**
- [ ] **Manual Recording**: Start/stop recording with button
- [ ] **Recording Indicator**: Visual "REC" indicator during recording
- [ ] **File Storage**: Recordings saved to device storage
- [ ] **Recording Types**: Continuous, motion-triggered, manual modes
- [ ] **Storage Management**: Automatic cleanup of old recordings

### **✅ Motion Detection**
- [ ] **Motion Detection**: Frame differencing algorithm works
- [ ] **Sensitivity Control**: Adjustable sensitivity (0-100)
- [ ] **Detection Zones**: Define specific areas for monitoring
- [ ] **Motion Events**: Proper event logging and handling
- [ ] **Auto Recording**: Motion-triggered recording starts automatically

### **✅ Background Services**
- [ ] **Camera Service**: Foreground service runs in background
- [ ] **Service Notifications**: Persistent notification shows service status
- [ ] **Auto-start**: Service starts on device boot (if enabled)
- [ ] **Service Controls**: Start/stop service from UI
- [ ] **Wake Lock**: Device stays awake during camera operation

### **✅ Power Management**
- [ ] **Battery Monitoring**: Real-time battery level display
- [ ] **Thermal Management**: Overheating detection and warnings
- [ ] **Power Modes**: Automatic quality adjustment based on battery
- [ ] **Battery Health**: Visual indicators for battery status
- [ ] **Power Optimization**: Reduced quality when battery is low

### **✅ Notifications**
- [ ] **Motion Notifications**: Push notifications for motion events
- [ ] **Device Status**: Online/offline notifications
- [ ] **Battery Alerts**: Low battery notifications
- [ ] **Storage Alerts**: Storage full notifications
- [ ] **Notification Channels**: Proper notification organization

### **✅ UI/UX Testing**
- [ ] **Camera Controls**: Intuitive button layout and functionality
- [ ] **Status Indicators**: Battery, recording, motion status display
- [ ] **Settings Dialog**: Resolution and quality configuration
- [ ] **Error Handling**: Proper error messages and recovery
- [ ] **Responsive Design**: UI adapts to different screen sizes

## 🔧 **Manual Testing Procedures**

### **Test Case 1: Basic Camera Operation**
1. Launch app and select "Camera Device" role
2. Grant camera permissions when prompted
3. Verify camera preview appears
4. Test camera switching (front/rear)
5. Test flash toggle
6. Verify camera state indicators

### **Test Case 2: Video Recording**
1. Start camera preview
2. Press record button to start recording
3. Verify "REC" indicator appears
4. Record for 10-30 seconds
5. Stop recording
6. Check that video file was created in storage

### **Test Case 3: Motion Detection**
1. Enable motion detection in settings
2. Set sensitivity to medium (50)
3. Move in front of camera
4. Verify motion event is detected
5. Check that motion-triggered recording starts
6. Verify motion notification is received

### **Test Case 4: Background Operation**
1. Start camera service
2. Minimize app (go to home screen)
3. Verify service notification appears
4. Check that camera continues operating
5. Return to app and verify state is maintained

### **Test Case 5: Power Management**
1. Monitor battery level display
2. Test with low battery (simulate if needed)
3. Verify quality reduction when battery is low
4. Check overheating warnings (if applicable)
5. Test power optimization features

### **Test Case 6: Auto-start Functionality**
1. Enable auto-start in settings
2. Restart device
3. Verify camera service starts automatically
4. Check that app opens to camera screen

## 🐛 **Common Issues and Solutions**

### **Issue: Camera Preview Not Showing**
**Solution:**
- Check camera permissions in device settings
- Verify camera is not being used by another app
- Restart the app and try again

### **Issue: Recording Not Working**
**Solution:**
- Check storage permissions
- Verify sufficient storage space
- Check if storage is full (app will auto-cleanup)

### **Issue: Motion Detection Too Sensitive/Not Sensitive Enough**
**Solution:**
- Adjust sensitivity in settings (0-100)
- Check lighting conditions
- Verify detection zones are set correctly

### **Issue: Service Not Starting**
**Solution:**
- Check battery optimization settings
- Verify auto-start permissions
- Check if device has background app restrictions

### **Issue: Notifications Not Working**
**Solution:**
- Verify Firebase configuration
- Check notification permissions
- Ensure FCM token is properly registered

## 📊 **Performance Testing**

### **Battery Usage**
- Monitor battery drain during continuous operation
- Test with different quality settings
- Verify power optimization features work

### **Memory Usage**
- Check memory consumption during recording
- Monitor for memory leaks during long sessions
- Test with multiple recording sessions

### **Storage Management**
- Test automatic cleanup of old recordings
- Verify storage limits are respected
- Check file organization and naming

### **Thermal Performance**
- Monitor device temperature during operation
- Test thermal throttling behavior
- Verify overheating protection works

## 🔒 **Security Testing**

### **Permission Handling**
- Verify all permissions are properly requested
- Test permission denial scenarios
- Check permission persistence

### **Data Privacy**
- Verify recordings are stored securely
- Check that sensitive data is not exposed
- Test backup exclusion rules

### **Authentication**
- Test login/logout functionality
- Verify session management
- Check for authentication bypasses

## 📱 **Device Compatibility Testing**

### **Test on Different Devices:**
- **High-end devices**: Test full feature set
- **Mid-range devices**: Verify performance optimization
- **Low-end devices**: Test with reduced quality settings
- **Different Android versions**: API 24+ compatibility

### **Screen Size Testing:**
- **Phone screens**: 5-7 inch displays
- **Tablet screens**: 8-12 inch displays
- **Different aspect ratios**: 16:9, 18:9, 21:9

## 🎯 **Success Criteria**

### **Phase 2 is considered successful when:**
- ✅ Camera preview works on all supported devices
- ✅ Video recording functions properly
- ✅ Motion detection is reliable and configurable
- ✅ Background services operate correctly
- ✅ Power management features work as designed
- ✅ Notifications are delivered properly
- ✅ UI is responsive and intuitive
- ✅ No critical crashes or data loss
- ✅ Performance is acceptable on target devices

## 📝 **Bug Reporting**

When reporting bugs, please include:
1. **Device information**: Model, Android version, screen size
2. **Steps to reproduce**: Detailed step-by-step instructions
3. **Expected behavior**: What should happen
4. **Actual behavior**: What actually happens
5. **Logs**: Relevant logcat output
6. **Screenshots/Videos**: Visual evidence if applicable

## 🚀 **Next Steps After Testing**

Once Phase 2 testing is complete and successful:
1. **Phase 3**: Implement Viewer Device & Real-time Streaming
2. **Device Pairing**: QR code-based pairing system
3. **WebRTC Integration**: Real-time video streaming
4. **Multi-camera Support**: Multiple camera management
5. **Cloud Integration**: Firebase backend services

---

**Happy Testing! 🎉**

The WebcamApp Phase 2 implementation provides a solid foundation for a professional camera application with comprehensive features for security monitoring, content creation, and remote surveillance.