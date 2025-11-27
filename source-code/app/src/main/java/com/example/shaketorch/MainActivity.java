package com.example.shaketorch;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ShakeTorchPrefs";
    private static final String SENSITIVITY_KEY = "sensitivity";
    private static final int DEFAULT_SENSITIVITY = 4;

    private SwitchMaterial serviceSwitch;
    private TextView statusText;
    private SeekBar sensitivitySeekBar;
    private SharedPreferences sharedPreferences;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    startShakeService();
                } else {
                    Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
                    serviceSwitch.setChecked(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupViews();
        setupListeners();
    }

    private void setupViews() {
        statusText = findViewById(R.id.statusTextView);
        serviceSwitch = findViewById(R.id.serviceSwitch);
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);

        int savedSensitivity = sharedPreferences.getInt(SENSITIVITY_KEY, DEFAULT_SENSITIVITY);
        sensitivitySeekBar.setProgress(savedSensitivity);
    }

    private void setupListeners() {
        serviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermissionsAndStartService();
            } else {
                stopShakeService();
            }
        });

        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt(SENSITIVITY_KEY, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (serviceSwitch.isChecked()) {
                    startShakeService();
                }
            }
        });

        findViewById(R.id.githubButton).setOnClickListener(v -> 
                openUrl("https://github.com/Lotverp"));

        findViewById(R.id.donateButton).setOnClickListener(v -> 
                openUrl("https://github.com/Lotverp/Lotverp/blob/main/donate.md"));
    }

    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void checkPermissionsAndStartService() {
        List<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            startShakeService();
        } else {
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    private void startShakeService() {
        Intent serviceIntent = new Intent(this, ShakeService.class);
        int sensitivity = sensitivitySeekBar.getProgress();
        serviceIntent.putExtra(SENSITIVITY_KEY, sensitivity);
        ContextCompat.startForegroundService(this, serviceIntent);
        updateUI(true);
    }

    private void stopShakeService() {
        Intent serviceIntent = new Intent(this, ShakeService.class);
        stopService(serviceIntent);
        updateUI(false);
    }

    private void updateUI(boolean isServiceActive) {
        if (isServiceActive) {
            statusText.setText("Service Active");
            serviceSwitch.setText("Deactivate Shake Detector");
        } else {
            statusText.setText("Service Disabled");
            serviceSwitch.setText("Activate Shake Detector");
        }
    }
}
