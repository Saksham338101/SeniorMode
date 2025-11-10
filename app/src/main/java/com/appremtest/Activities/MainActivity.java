package com.appremtest.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.appremtest.R;

/**
 * Senior-friendly launcher activity. Minimal, high-contrast UI with large buttons.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        // If Accessibility Service is not enabled, prompt user to enable it so global actions work
        if (!isAccessibilityServiceEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Accessibility")
                    .setMessage("To use Senior Mode global actions (Home/Back) please enable the accessibility service for this app. Would you like to open Accessibility settings now?")
                    .setPositiveButton("Open Settings", (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                    .setNeutralButton("No Thanks", (dialog, which) -> dialog.dismiss())
                    .show();
        }

    Button btnAccessibility = findViewById(R.id.btnAccessibilitySettings);
    Button btnContacts = findViewById(R.id.btnContacts);
    Button btnPhone = findViewById(R.id.btnPhone);
    Button btnEmergency = findViewById(R.id.btnEmergency);
    Button btnToggleOverlay = findViewById(R.id.btnToggleOverlay);
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        btnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            }
        });

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                startActivity(intent);
            }
        });

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                startActivity(intent);
            }
        });

        // No in-app Back button: rely on system/back gestures or floating handle where appropriate

        // Home handle toggle: start/stop persistent home handle and persist preference
        final String PREF_HANDLE_ENABLED = "pref_handle_enabled";
        boolean handleEnabled = prefs.getBoolean(PREF_HANDLE_ENABLED, false);
        btnToggleOverlay.setText(handleEnabled ? "Remove home handle" : "Add home handle");

        // On first run: if pref is true ensure handle is running (ask permission if needed)
        if (handleEnabled && !com.appremtest.Services.HomeHandleService.isRunning()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Overlay permission required")
                        .setMessage("Please grant the Draw over other apps permission so the home handle can be displayed.")
                        .setPositiveButton("Open Settings", (d, w) -> startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)))
                        .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                        .show();
            } else {
                Intent svc = new Intent(MainActivity.this, com.appremtest.Services.HomeHandleService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(svc); else startService(svc);
            }
        }

        btnToggleOverlay.setOnClickListener(v -> {
            boolean current = prefs.getBoolean(PREF_HANDLE_ENABLED, false);
            if (current) {
                // stop handle
                Intent svc = new Intent(MainActivity.this, com.appremtest.Services.HomeHandleService.class);
                stopService(svc);
                prefs.edit().putBoolean(PREF_HANDLE_ENABLED, false).apply();
                btnToggleOverlay.setText("Add home handle");
            } else {
                // request overlay permission if needed
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(MainActivity.this)) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Overlay permission required")
                            .setMessage("Please grant the Draw over other apps permission so the home handle can be displayed.")
                            .setPositiveButton("Open Settings", (d, w) -> startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)))
                            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                            .show();
                    return;
                }

                Intent svc = new Intent(MainActivity.this, com.appremtest.Services.HomeHandleService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(svc); else startService(svc);
                prefs.edit().putBoolean(PREF_HANDLE_ENABLED, true).apply();
                btnToggleOverlay.setText("Remove home handle");
            }
        });
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            String enabledServices = Secure.getString(getContentResolver(), Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServices == null) return false;
            return enabledServices.toLowerCase().contains(getPackageName().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
}


