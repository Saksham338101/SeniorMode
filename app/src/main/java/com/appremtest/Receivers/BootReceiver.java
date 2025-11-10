package com.appremtest.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Receiver to auto-start the small HomeHandleService after device boot when the user enabled it.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String PREF_HANDLE_ENABLED = "pref_handle_enabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enabled = prefs.getBoolean(PREF_HANDLE_ENABLED, false);
                if (enabled) {
                    Intent svc = new Intent(context, com.appremtest.Services.HomeHandleService.class);
                    // need FLAG_ACTIVITY_NEW_TASK only for activities; for services just startService
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(svc);
                    } else {
                        context.startService(svc);
                    }
                    Log.i(TAG, "HomeHandleService started on boot (pref enabled)");
                } else {
                    Log.i(TAG, "HomeHandleService not started on boot (pref disabled)");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to auto-start HomeHandleService: " + e.getMessage());
            }
        }
    }
}
