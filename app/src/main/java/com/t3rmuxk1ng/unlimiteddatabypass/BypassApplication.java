package com.t3rmuxk1ng.unlimiteddatabypass;

import android.app.Application;
import android.util.Log;

/**
 * APPLICATION CLASS - GLOBAL EXCEPTION HANDLER
 * Prevents app crashes and logs errors
 */
public class BypassApplication extends Application {

    private static final String TAG = "BypassApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), throwable);
            // Log but don't crash - let the app continue
        });
        
        Log.d(TAG, "Application started - GOD TIER EDITION");
    }
}
