package com.appremtest.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.appremtest.R;

/**
 * Small overlay activity for emergency actions. Offers Dial (no permission) and Direct Call (CALL_PHONE).
 */
public class EmergencyOverlayActivity extends AppCompatActivity {

    private static final int REQ_CALL_PHONE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlay_emergency);

        Button btnDial = findViewById(R.id.btn_dial_emergency);
        Button btnCall = findViewById(R.id.btn_call_emergency);
        Button btnClose = findViewById(R.id.btn_emergency_close);

        btnDial.setOnClickListener(v -> {
            // Stop overlay so the dialer is visible
            stopService(new Intent(this, com.appremtest.Services.OverlayService.class));
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL_PHONE);
            } else {
                // Stop overlay before making the call so the Call UI is visible
                stopService(new Intent(this, com.appremtest.Services.OverlayService.class));
                makeCall();
                finish();
            }
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            }
        }
    }
}
