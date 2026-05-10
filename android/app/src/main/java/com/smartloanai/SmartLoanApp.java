package com.smartloanai;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

/**
 * Application class for Smart Loan AI.
 * Handles global app initialization and theme management.
 */
public class SmartLoanApp extends Application {

    private static SmartLoanApp instance;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Apply saved theme preference
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static SmartLoanApp getInstance() {
        return instance;
    }

    /**
     * Get the stored JWT token.
     */
    public String getAuthToken() {
        return prefs.getString("auth_token", null);
    }

    /**
     * Store JWT token after login.
     */
    public void setAuthToken(String token) {
        prefs.edit().putString("auth_token", token).apply();
    }

    /**
     * Get stored user ID.
     */
    public String getUserId() {
        return prefs.getString("user_id", null);
    }

    /**
     * Store user data after login.
     */
    public void setUserData(String userId, String name, String email) {
        prefs.edit()
            .putString("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .apply();
    }

    public String getUserName() {
        return prefs.getString("user_name", "User");
    }

    public String getUserEmail() {
        return prefs.getString("user_email", "");
    }

    /**
     * Check if user is logged in.
     */
    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }

    /**
     * Clear all auth data (logout).
     */
    public void logout() {
        prefs.edit()
            .remove("auth_token")
            .remove("user_id")
            .remove("user_name")
            .remove("user_email")
            .apply();
    }

    /**
     * Check if onboarding has been completed.
     */
    public boolean isOnboardingComplete() {
        return prefs.getBoolean("onboarding_complete", false);
    }

    public void setOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_complete", true).apply();
    }

    /**
     * Toggle dark mode.
     */
    public void toggleDarkMode() {
        boolean current = prefs.getBoolean("dark_mode", false);
        prefs.edit().putBoolean("dark_mode", !current).apply();
        AppCompatDelegate.setDefaultNightMode(
            !current ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public boolean isDarkMode() {
        return prefs.getBoolean("dark_mode", false);
    }
}
