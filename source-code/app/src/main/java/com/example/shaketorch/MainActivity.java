package com.example.shaketorch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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

    private SwitchMaterial serviceSwitch;
    private TextView statusText;

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

        statusText = findViewById(R.id.statusTextView);
        serviceSwitch = findViewById(R.id.serviceSwitch);
        ImageButton githubButton = findViewById(R.id.githubButton);
        Button donateButton = findViewById(R.id.donateButton);

        serviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermissionsAndStartService();
            } else {
                stopShakeService();
            }
        });

        githubButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Lotverp"));
            startActivity(browserIntent);
        });

        donateButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Lotverp/Lotverp/blob/main/donate.md"));
            startActivity(browserIntent);
        });
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
        ContextCompat.startForegroundService(this, serviceIntent);
        statusText.setText("Service Active");
        serviceSwitch.setText("Deactivate Shake Detector");
    }

    private void stopShakeService() {
        Intent serviceIntent = new Intent(this, ShakeService.class);
        stopService(serviceIntent);
        statusText.setText("Service Disabled");
        serviceSwitch.setText("Activate Shake Detector");
    }
}
