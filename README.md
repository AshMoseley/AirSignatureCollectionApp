# Air Signature Data Collector

## Overview
The Air Signature Data Collector is an Android application that captures accelerometer data from a mobile device and records it in a CSV file with labeled columns. This app is useful for collecting sensor data for analysis, experiments, or machine learning projects.

## Features
- Captures real-time accelerometer data (X, Y, Z axes).
- Records data into a CSV file stored in the device's external storage.
- Displays current and maximum values of accelerometer readings on the UI.
- Simple start/stop button to control data recording.
- Handles permission requests for writing to external storage.

## Requirements
- Android device running Android 6.0 (Marshmallow) or higher.
- External storage permission for writing the CSV file.

## Installation
1. Clone or download the repository.
   ```bash
   git clone https://github.com/your-username/air-signature-data-collector.git
    ```
2. Open the project in Android Studio.
3. Build and run the application on an Android device or emulator with accelerometer support

## Usage
1. Launch the app on your Android device.
2. Ensure that the app has permission to write to external storage. If not, the app will request permission on startup.
3. Click the "Start" button to begin recording accelerometer data.
4. The app will record data for 5 seconds and save it to a CSV file in the Documents directory of the external storage.
5. Click the "Stop" button to manually stop recording before the 5-second interval.
6. The CSV file will contain columns labeled x1,y1,z1,...,x25,y25,z25 representing the recorded accelerometer data.

## File Structure
The recorded CSV file is saved in the Documents directory of the external storage with the filename data.csv. Each row in the CSV file corresponds to a set of accelerometer readings in the format:
  ```bash
  x1,y1,z1,x2,y2,z2,...,x25,y25,z25
  ```

## Code Explanation
### 'MainActivity.java'
- 'onCreate': Initializes the app, checks for storage permissions, and sets up UI components and sensor manager.
- 'onResume' / 'onPause': Registers and unregisters the accelerometer sensor listener.
- 'checkWriteStoragePermission': Requests storage permission if not already granted.
- 'initializeViews': Links the UI components to their respective views.
- 'onRequestPermissionsResult': Handles the result of the storage permission request.
- 'onSensorChanged': Captures the accelerometer data and updates the UI and CSV file.
- 'startRecording': Starts the data recording process and initializes the CSV file.
- 'stopRecording': Stops the data recording process and closes the CSV file writer.

## Permissions
The app requires the following permission in AndroidManifest.xml:
  ```bash
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
  ```

## Notes
- The app is designed to record accelerometer data for a fixed interval of 5 seconds. You can adjust this duration by modifying the delay in the 'startRecording' method.
- Ensure that the device has enough storage space and the Documents directory is accessible.
  
## Troubleshooting
- If the app crashes or does not record data, ensure that storage permission is granted and the device has an accelerometer sensor.
- Check the logcat for any error messages or stack traces to identify issues.
