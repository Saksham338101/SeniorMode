package com.appremtest.Services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.appremtest.R;

/**
 * Small floating home handle that relaunches the full overlay when tapped.
 */
public class HomeHandleService extends Service {

    private WindowManager windowManager;
    private View handleView;
    private static volatile boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        handleView = inflater.inflate(R.layout.overlay_handle, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        layoutFlag,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

    // place handle near bottom-end (bottom-right) with a margin
    params.gravity = Gravity.BOTTOM | Gravity.END;
    final int margin = (int) (24 * getResources().getDisplayMetrics().density);
    params.x = margin; // offset from right
    params.y = margin; // offset from bottom

        // draggable
        handleView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (windowManager != null) windowManager.updateViewLayout(handleView, params);
                        return true;
                }
                return false;
            }
        });

    Button btn = handleView.findViewById(R.id.handle_button);
        btn.setOnClickListener(v -> {
            // Launch the Senior Mode app (MainActivity) from the handle and keep the handle running
            try {
                Intent intent = new Intent(this, com.appremtest.Activities.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (Exception e) {
                // fallback: try to start overlay service if activity launch fails
                Intent svc = new Intent(this, OverlayService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc);
                else startService(svc);
            }
            // do NOT stopSelf(); keep the handle running so user can re-open the app later
        });

        try {
            windowManager.addView(handleView, params);
        } catch (Exception e) {
            Log.w("HomeHandleService", "Could not add handle view: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handleView != null && windowManager != null) {
            try { windowManager.removeView(handleView); } catch (Exception ignored) {}
            handleView = null;
        }
        isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    public static boolean isRunning() { return isRunning; }
}
