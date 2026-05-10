package com.smartloanai.ui.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;
import com.smartloanai.ui.auth.LoginActivity;
import com.smartloanai.ui.loan.LoanFormFragment;
import com.smartloanai.ui.chatbot.ChatbotFragment;
import com.smartloanai.ui.budget.BudgetFragment;
import com.smartloanai.ui.profile.ProfileFragment;

/**
 * Main activity with bottom navigation hosting 5 fragments.
 */
public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check auth
        if (!SmartLoanApp.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_loan) {
                fragment = new LoanFormFragment();
            } else if (itemId == R.id.nav_chat) {
                fragment = new ChatbotFragment();
            } else if (itemId == R.id.nav_budget) {
                fragment = new BudgetFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
}
