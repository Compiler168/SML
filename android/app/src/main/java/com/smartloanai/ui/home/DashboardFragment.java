package com.smartloanai.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;

/**
 * Dashboard fragment - main home screen with quick action cards.
 */
public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Set greeting
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        String name = SmartLoanApp.getInstance().getUserName();
        tvGreeting.setText("Hello, " + name + "! 👋");

        // Quick action cards
        View cardLoan = view.findViewById(R.id.card_loan_check);
        View cardChat = view.findViewById(R.id.card_ai_chat);
        View cardBudget = view.findViewById(R.id.card_budget);
        View cardRecommend = view.findViewById(R.id.card_recommendations);

        if (cardLoan != null) {
            cardLoan.setOnClickListener(v -> navigateToTab(R.id.nav_loan));
        }
        if (cardChat != null) {
            cardChat.setOnClickListener(v -> navigateToTab(R.id.nav_chat));
        }
        if (cardBudget != null) {
            cardBudget.setOnClickListener(v -> navigateToTab(R.id.nav_budget));
        }

        return view;
    }

    private void navigateToTab(int tabId) {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView nav =
                getActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) {
                nav.setSelectedItemId(tabId);
            }
        }
    }
}
