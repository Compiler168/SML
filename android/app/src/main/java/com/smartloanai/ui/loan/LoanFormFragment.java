package com.smartloanai.ui.loan;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartloanai.R;
import com.smartloanai.network.RetrofitClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Loan eligibility form and result fragment.
 */
public class LoanFormFragment extends Fragment {

    private TextInputEditText etAge, etIncome, etExpenses, etDebts, etLoanAmount, etLoanTerm, etCreditScore;
    private Spinner spGender, spEducation, spEmployment;
    private Button btnCheck;
    private ProgressBar progressBar;

    // Result views
    private View resultLayout;
    private TextView tvResult, tvProbability, tvRiskLevel, tvHealthScore;
    private LinearLayout suggestionsContainer;
    private CircularProgressIndicator progressProbability;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loan_form, container, false);

        // Input fields
        etAge = view.findViewById(R.id.et_age);
        etIncome = view.findViewById(R.id.et_income);
        etExpenses = view.findViewById(R.id.et_expenses);
        etDebts = view.findViewById(R.id.et_debts);
        etLoanAmount = view.findViewById(R.id.et_loan_amount);
        etLoanTerm = view.findViewById(R.id.et_loan_term);
        etCreditScore = view.findViewById(R.id.et_credit_score);
        btnCheck = view.findViewById(R.id.btn_check);
        progressBar = view.findViewById(R.id.progress_bar);

        // Spinners
        spGender = view.findViewById(R.id.sp_gender);
        spEducation = view.findViewById(R.id.sp_education);
        spEmployment = view.findViewById(R.id.sp_employment);

        setupSpinners();

        // Result views
        resultLayout = view.findViewById(R.id.result_layout);
        tvResult = view.findViewById(R.id.tv_result);
        tvProbability = view.findViewById(R.id.tv_probability);
        tvRiskLevel = view.findViewById(R.id.tv_risk_level);
        tvHealthScore = view.findViewById(R.id.tv_health_score);
        suggestionsContainer = view.findViewById(R.id.suggestions_container);
        progressProbability = view.findViewById(R.id.progress_probability);

        btnCheck.setOnClickListener(v -> submitForm());

        return view;
    }

    private void setupSpinners() {
        String[] genders = {"Male", "Female", "Other"};
        String[] education = {"High School", "Associate", "Bachelor", "Master", "PhD"};
        String[] employment = {"Employed", "Self-Employed", "Unemployed", "Part-Time", "Retired"};

        spGender.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders));
        spEducation.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, education));
        spEmployment.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, employment));
    }

    private void submitForm() {
        // Validate
        String ageStr = getText(etAge);
        String incomeStr = getText(etIncome);
        String expensesStr = getText(etExpenses);
        String debtsStr = getText(etDebts);
        String loanStr = getText(etLoanAmount);
        String termStr = getText(etLoanTerm);
        String creditStr = getText(etCreditScore);

        if (ageStr.isEmpty() || incomeStr.isEmpty() || loanStr.isEmpty() || creditStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        resultLayout.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("age", Integer.parseInt(ageStr));
        body.put("gender", spGender.getSelectedItem().toString());
        body.put("education", spEducation.getSelectedItem().toString());
        body.put("employment_status", spEmployment.getSelectedItem().toString());
        body.put("annual_income", Double.parseDouble(incomeStr));
        body.put("monthly_expenses", expensesStr.isEmpty() ? 0.0 : Double.parseDouble(expensesStr));
        body.put("existing_debts", debtsStr.isEmpty() ? 0.0 : Double.parseDouble(debtsStr));
        body.put("loan_amount", Double.parseDouble(loanStr));
        body.put("loan_term", termStr.isEmpty() ? 36 : Integer.parseInt(termStr));
        body.put("credit_score", Integer.parseInt(creditStr));

        RetrofitClient.getInstance().getApi().predictLoan(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayResult(response.body().getAsJsonObject("data"));
                } else {
                    Toast.makeText(getContext(), "Prediction failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayResult(JsonObject data) {
        resultLayout.setVisibility(View.VISIBLE);

        boolean approved = data.get("approved").getAsBoolean();
        double probability = data.get("probability").getAsDouble();
        String riskLevel = data.get("risk_level").getAsString();
        String riskColor = data.get("risk_color").getAsString();

        // Result status
        tvResult.setText(approved ? "✅ APPROVED" : "❌ REJECTED");
        tvResult.setTextColor(Color.parseColor(approved ? "#16A34A" : "#DC2626"));

        // Probability
        int probPercent = (int) (probability * 100);
        tvProbability.setText(String.format("%d%%", probPercent));
        
        // Animate circular progress indicator
        progressProbability.setIndicatorColor(Color.parseColor(approved ? "#16A34A" : "#DC2626"));
        progressProbability.setProgressCompat(probPercent, true);

        // Risk level
        tvRiskLevel.setText(riskLevel);
        tvRiskLevel.setTextColor(Color.parseColor(riskColor));

        // Health score
        JsonObject health = data.getAsJsonObject("financial_health");
        if (health != null) {
            double score = health.get("health_score").getAsDouble();
            String grade = health.get("grade").getAsString();
            tvHealthScore.setText(String.format("%.0f/100 (Grade: %s)", score, grade));
        }

        // Suggestions
        suggestionsContainer.removeAllViews();
        JsonArray suggestions = data.getAsJsonArray("suggestions");
        if (suggestions != null) {
            for (int i = 0; i < suggestions.size(); i++) {
                JsonObject sug = suggestions.get(i).getAsJsonObject();
                TextView tv = new TextView(getContext());
                tv.setText("💡 " + sug.get("suggestion").getAsString());
                tv.setTextSize(14);
                tv.setPadding(0, 8, 0, 8);
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                suggestionsContainer.addView(tv);
            }
        }

        // Scroll to result
        resultLayout.requestFocus();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnCheck.setEnabled(!loading);
    }
}
