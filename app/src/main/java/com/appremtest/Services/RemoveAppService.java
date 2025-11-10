/*
 * Minimal AccessibilityService for Senior Mode
 * (originally RemoveAppService) — repurposed to provide a safe starting point
 * for accessibility features without performing uninstall actions.
 */

package com.appremtest.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Repurposed AccessibilityService for Senior Mode.
 * Provides a safe bridge to perform global actions and observe preference changes
 * for features such as large text or high-contrast mode.
 */
public class RemoveAppService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static volatile RemoveAppService instance;
    private SharedPreferences prefs;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        Log.i("SeniorModeService", "Accessibility service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Minimal: could observe window changes and optionally simplify confirmations
        // Future: inspect event types and apply UI simplification rules based on prefs.
    }

    @Override
    public void onInterrupt() {
        // No-op
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }
        instance = null;
        Log.i("SeniorModeService", "Accessibility service destroyed");
    }

    /**
     * Safely let other components ask the service to perform a global action.
     * Returns true if the action was dispatched.
     */
    public static boolean performGlobalActionFromClient(int action) {
        RemoveAppService svc = instance;
        if (svc == null) return false;
        return svc.performGlobalAction(action);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // React to preference changes if necessary (e.g., toggle internal flags)
        Log.i("SeniorModeService", "Preference changed: " + key);
    }

}
