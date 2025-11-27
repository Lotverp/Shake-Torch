package com.example.shaketorch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class ShakeService extends Service implements SensorEventListener {

    private static final String SENSITIVITY_KEY = "sensitivity";
    private static final int DEFAULT_SENSITIVITY = 4;
    private static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    private static final String NOTIFICATION_CHANNEL_ID = "SHAKE_CHANNEL";

    // Higher threshold = less sensitive
    private static final int MAX_SHAKE_THRESHOLD = 26;
    // Lower threshold = more sensitive
    private static final int MIN_SHAKE_THRESHOLD = 8;

    private SensorManager sensorManager;
    private CameraManager cameraManager;
    private String cameraId;

    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean isFlashOn = false;
    private long lastShakeTime = 0;
    private int shakeThreshold;

    @Override
    public void onCreate() {
        super.onCreate();
        setupSensor();
        setupCamera();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int sensitivity = intent.getIntExtra(SENSITIVITY_KEY, DEFAULT_SENSITIVITY);
        // We map sensitivity [0..9] to threshold [26..8].
        // Lower sensitivity value means less sensitive, thus higher threshold.
        shakeThreshold = MAX_SHAKE_THRESHOLD - (sensitivity * 2);

        startForegroundService();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) < MIN_TIME_BETWEEN_SHAKES) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > shakeThreshold) {
                lastShakeTime = currentTime;
                toggleFlashlight();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnOffFlashlight();
        sensorManager.unregisterListener(this);
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    private void setupCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startForegroundService() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }
    
    private void toggleFlashlight() {
        try {
            if (isFlashOn) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlashlight() {
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, 
                    "Shake Service Channel", 
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
