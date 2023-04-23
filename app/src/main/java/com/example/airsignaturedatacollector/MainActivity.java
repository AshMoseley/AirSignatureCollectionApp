package com.example.airsignaturedatacollector;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_CODE_WRITE_STORAGE = 123;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private TextView currentX;
    private TextView currentY;
    private TextView currentZ;
    private TextView maxX;
    private TextView maxY;
    private TextView maxZ;

    private Button startButton;

    private boolean isRecording = false;

    private File file;
    private FileWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkWriteStoragePermission();
        initializeViews();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
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

    private void initializeViews() {
        currentX = findViewById(R.id.currentX);
        currentY = findViewById(R.id.currentY);
        currentZ = findViewById(R.id.currentZ);
        maxX = findViewById(R.id.maxX);
        maxY = findViewById(R.id.maxY);
        maxZ = findViewById(R.id.maxZ);
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
        if (isRecording) {
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

            try {
                writer.append(event.values[0] + "," + event.values[1] + "," + event.values[2] + ",");
               // writer.append(String.format(Locale.US, ",%.2f,%.2f,%.2f", event.values[0], event.values[1], event.values[2]));
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startRecording() {
        isRecording = true;
        startButton.setText("Stop");
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        try {
            String filename = "data.csv";
            file = new File(dir, filename);
            writer = new FileWriter(file,true);
            StringBuilder sb = new StringBuilder();
            if (file.length() == 0) {
            for (int i = 1; i <= 25; i++) {
                sb.append(String.format("x%d,y%d,z%d", i, i, i));
                if (i != 25) {
                    sb.append(",");
                  }
                }
            }
            sb.append("\n");
            writer.append(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecording();
            }
        }, 5000);
    }

    private void stopRecording() {
        isRecording = false;
        startButton.setText("Start");
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Data saved to " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
