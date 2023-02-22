package com.example.airsignaturedatacollector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQUEST_CODE_WRITE_STORAGE = 123;
    private static final int SENSOR_DELAY_TIME = SensorManager.SENSOR_DELAY_NORMAL;
    private static final long VIBRATION_DURATION = 50;
    private static final long COLLECTION_DURATION = 3000;

    private boolean isCollectingData = false;
    private Handler handler = new Handler();

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float vibrateThreshold = 0;

    private TextView currentX;
    private TextView currentY;
    private TextView currentZ;
    private TextView maxX;
    private TextView maxY;
    private TextView maxZ;

    private Vibrator vibrator;

    private Button startButton; // New variable to hold the start button
    private int fileCounter = 1; // New variable to keep track of the file count


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        checkWriteStoragePermission();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            Toast.makeText(getBaseContext(), "Accelerometer sensor not found!", Toast.LENGTH_LONG).show();
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startButton = findViewById(R.id.start_button); // Find the start button by its ID
        startButton.setOnClickListener(new View.OnClickListener() { // Add a click listener to the button
            @Override
            public void onClick(View v) {
                startDataCollection(); // Call the method to start data collection
            }
        });
    }
    private Runnable stopDataCollectionTask = new Runnable() {
        @Override
        public void run() {
            stopDataCollection();
        }
    };
    private void startDataCollection() {
        if (isCollectingData) {
            return;
        }
        fileCounter++; // Increment the file count
        saveDataToFileHeader(); // Save a new file header
        Toast.makeText(getBaseContext(), "Data collection started. File #" + fileCounter + " created.", Toast.LENGTH_LONG).show();
        isCollectingData = true;
        handler.postDelayed(stopDataCollectionTask, COLLECTION_DURATION);
    }

    private void stopDataCollection() {
        isCollectingData = false;
        Toast.makeText(getBaseContext(), "Data collection stopped.", Toast.LENGTH_LONG).show();
    }

    private void initializeViews() {
        currentX = findViewById(R.id.currentX);
        currentY = findViewById(R.id.currentY);
        currentZ = findViewById(R.id.currentZ);
        maxX = findViewById(R.id.maxX);
        maxY = findViewById(R.id.maxY);
        maxZ = findViewById(R.id.maxZ);
    }

    private void checkWriteStoragePermission() {
        int hasWriteStoragePermission = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWriteStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
            }
        } else {
            Toast.makeText(getBaseContext(), "Storage write permission is already granted.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getBaseContext(), "Storage write permission granted.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "Storage write permission denied. Data collection cannot proceed.", Toast.LENGTH_LONG).show();
                startButton.setEnabled(false); // Disable the start button if permission is denied
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX.setText(String.format("%.2f", event.values[0]));
        currentY.setText(String.format("%.2f", event.values[1]));
        currentZ.setText(String.format("%.2f", event.values[2]));

        float deltaX = Math.abs(lastX - event.values[0]);
        float deltaY = Math.abs(lastY - event.values[1]);
        float deltaZ = Math.abs(lastZ - event.values[2]);

        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(String.format("%.2f", deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(String.format("%.2f", deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(String.format("%.2f", deltaZMax));
        }

        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        if (deltaX > vibrateThreshold || deltaY > vibrateThreshold || deltaZ > vibrateThreshold) {
            vibrator.vibrate(VIBRATION_DURATION);
        }

        saveDataToFile(event); // Save the data to the file
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void saveDataToFileHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String fileName = "data_" + fileCounter + "_" + currentDateandTime + ".csv";

        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.append("Time,X,Y,Z\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile(SensorEvent event) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String fileName = "data_" + fileCounter + "_" + currentDateandTime.substring(0, 10) + ".csv";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
        FileWriter writer;
        try {
            writer = new FileWriter(file, true);
            writer.append(event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}


