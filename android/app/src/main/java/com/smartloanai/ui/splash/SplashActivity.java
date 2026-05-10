package com.smartloanai.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;
import com.smartloanai.ui.auth.LoginActivity;
import com.smartloanai.ui.home.HomeActivity;

/**
 * Splash screen activity - shows app logo and navigates based on auth state.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animate elements
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);
        ImageView logo = findViewById(R.id.splash_logo);

        // Fade-in animation for logo
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);

        // Delayed fade-in for text
        AlphaAnimation textFade = new AlphaAnimation(0f, 1f);
        textFade.setDuration(800);
        textFade.setStartOffset(600);
        textFade.setFillAfter(true);

        if (logo != null) logo.startAnimation(fadeIn);
        if (appName != null) appName.startAnimation(textFade);
        if (tagline != null) tagline.startAnimation(textFade);

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SmartLoanApp app = SmartLoanApp.getInstance();
            Intent intent;

            if (!app.isOnboardingComplete()) {
                intent = new Intent(this, OnboardingActivity.class);
            } else if (app.isLoggedIn()) {
                intent = new Intent(this, HomeActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DELAY);
    }
}
