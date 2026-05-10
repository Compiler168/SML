package com.smartloanai.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;
import com.smartloanai.ui.admin.AdminActivity;
import com.smartloanai.ui.auth.LoginActivity;

/**
 * Profile fragment with user info, theme toggle, and logout.
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        SmartLoanApp app = SmartLoanApp.getInstance();

        // User info
        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvEmail = view.findViewById(R.id.tv_profile_email);
        TextView tvInitial = view.findViewById(R.id.tv_profile_initial);

        tvName.setText(app.getUserName());
        tvEmail.setText(app.getUserEmail());

        String name = app.getUserName();
        tvInitial.setText(name.isEmpty() ? "U" : name.substring(0, 1).toUpperCase());

        // Dark mode toggle
        SwitchMaterial darkSwitch = view.findViewById(R.id.switch_dark_mode);
        darkSwitch.setChecked(app.isDarkMode());
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.toggleDarkMode();
        });

        // Admin button (visible for all users in dev mode)
        View btnAdmin = view.findViewById(R.id.btn_admin);
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), AdminActivity.class));
            });
        }

        // Logout
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            app.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
