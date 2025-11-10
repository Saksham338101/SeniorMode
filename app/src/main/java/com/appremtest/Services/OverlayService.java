package com.appremtest.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.appremtest.R;

/**
 * Minimal overlay service that creates a small floating toolbar over other apps.
 * Buttons leverage the AccessibilityService global actions when available.
 */
public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private static volatile boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // On O+ a foreground notification is required when starting via startForegroundService
        final String CHANNEL_ID = "senior_mode_overlay";
        final int NOTIF_ID = 0x99;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Senior Mode Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Overlay running");
            if (nm != null) nm.createNotificationChannel(channel);
        }
        Intent piIntent = new Intent(this, com.appremtest.Activities.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, piIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Senior Mode")
                .setContentText("Overlay active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
        try {
            startForeground(NOTIF_ID, notif);
        } catch (Exception e) {
            Log.w("OverlayService", "startForeground failed: " + e.getMessage());
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_fullscreen, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                // allow touch events to be handled by overlay controls
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        // Wire overlay controls to perform the same actions as MainActivity
        overlayView.findViewById(R.id.btnAccessibilitySettings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        overlayView.findViewById(R.id.btnContacts).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.provider.ContactsContract.Contacts.CONTENT_URI);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        overlayView.findViewById(R.id.btnPhone).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        overlayView.findViewById(R.id.btnEmergency).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Home and Back removed from overlay UI; floating handle provides Home, accessibility service may provide global actions if enabled

        overlayView.findViewById(R.id.btn_overlay_close).setOnClickListener(v -> stopSelf());

        // Hook preference toggles so overlay can store choices (they persist via SharedPreferences)
        // Note: overlay switch state is persisted and read by MainActivity; the service listens for changes.

        try {
            windowManager.addView(overlayView, params);
            isRunning = true;
        } catch (Exception e) {
            Log.w("OverlayService", "Could not add overlay view: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {
            }
            overlayView = null;
        }
        isRunning = false;
        // When the fullscreen overlay stops, start a small home handle so the user can relaunch it
        try {
            Intent svc = new Intent(this, HomeHandleService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc);
            else startService(svc);
        } catch (Exception e) {
            Log.w("OverlayService", "Could not start HomeHandleService: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
