package com.smartloanai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;
import com.smartloanai.network.RetrofitClient;
import com.smartloanai.ui.home.HomeActivity;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Signup screen for new user registration.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnSignup;
    private ProgressBar progressBar;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);
        btnSignup = findViewById(R.id.btn_signup);
        progressBar = findViewById(R.id.progress_bar);
        tvLogin = findViewById(R.id.tv_login);

        btnSignup.setOnClickListener(v -> performSignup());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void performSignup() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirm = getText(etConfirmPassword);
        String phone = getText(etPhone);

        // Validation
        if (name.isEmpty()) { etName.setError("Name is required"); return; }
        if (email.isEmpty()) { etEmail.setError("Email is required"); return; }
        if (password.isEmpty()) { etPassword.setError("Password is required"); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError("Passwords do not match"); return; }

        setLoading(true);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        if (!phone.isEmpty()) body.put("phone", phone);

        RetrofitClient.getInstance().getApi().signup(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    String token = data.get("access_token").getAsString();
                    JsonObject user = data.getAsJsonObject("user");

                    SmartLoanApp app = SmartLoanApp.getInstance();
                    app.setAuthToken(token);
                    app.setUserData(
                        user.get("id").getAsString(),
                        user.get("name").getAsString(),
                        user.get("email").getAsString()
                    );

                    Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignupActivity.this, "Signup failed. Email may already exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
        btnSignup.setText(loading ? "Creating Account..." : getString(R.string.signup));
    }
}
