package com.smartloanai.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.smartloanai.R;
import com.smartloanai.network.RetrofitClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Budget analysis fragment with income/expense tracking and EMI calculator.
 */
public class BudgetFragment extends Fragment {

    private TextInputEditText etIncome, etExpenses, etDebts, etCreditScore;
    private TextInputEditText etEmiLoan, etEmiRate, etEmiTerm;
    private Button btnAnalyze, btnCalcEmi;
    private ProgressBar progressBar;
    private View resultSection, emiResultSection;
    private TextView tvHealthScore, tvGrade, tvSavingsRate, tvDti, tvMonthlySavings;
    private TextView tvEmi, tvTotalPayment, tvTotalInterest, tvAffordable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Budget inputs
        etIncome = view.findViewById(R.id.et_budget_income);
        etExpenses = view.findViewById(R.id.et_budget_expenses);
        etDebts = view.findViewById(R.id.et_budget_debts);
        etCreditScore = view.findViewById(R.id.et_budget_credit);
        btnAnalyze = view.findViewById(R.id.btn_analyze);
        progressBar = view.findViewById(R.id.progress_bar);

        // Budget results
        resultSection = view.findViewById(R.id.budget_result_section);
        tvHealthScore = view.findViewById(R.id.tv_budget_health_score);
        tvGrade = view.findViewById(R.id.tv_budget_grade);
        tvSavingsRate = view.findViewById(R.id.tv_savings_rate);
        tvDti = view.findViewById(R.id.tv_dti_ratio);
        tvMonthlySavings = view.findViewById(R.id.tv_monthly_savings);

        // EMI inputs
        etEmiLoan = view.findViewById(R.id.et_emi_loan);
        etEmiRate = view.findViewById(R.id.et_emi_rate);
        etEmiTerm = view.findViewById(R.id.et_emi_term);
        btnCalcEmi = view.findViewById(R.id.btn_calc_emi);

        // EMI results
        emiResultSection = view.findViewById(R.id.emi_result_section);
        tvEmi = view.findViewById(R.id.tv_emi_amount);
        tvTotalPayment = view.findViewById(R.id.tv_total_payment);
        tvTotalInterest = view.findViewById(R.id.tv_total_interest);
        tvAffordable = view.findViewById(R.id.tv_affordable);

        btnAnalyze.setOnClickListener(v -> analyzeBudget());
        btnCalcEmi.setOnClickListener(v -> calculateEmi());

        return view;
    }

    private void analyzeBudget() {
        String incomeStr = getText(etIncome);
        String expensesStr = getText(etExpenses);

        if (incomeStr.isEmpty()) { etIncome.setError("Required"); return; }
        if (expensesStr.isEmpty()) { etExpenses.setError("Required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        resultSection.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("annual_income", Double.parseDouble(incomeStr));
        body.put("monthly_expenses", Double.parseDouble(expensesStr));
        body.put("existing_debts", getText(etDebts).isEmpty() ? 0.0 : Double.parseDouble(getText(etDebts)));
        body.put("credit_score", getText(etCreditScore).isEmpty() ? 650 : Integer.parseInt(getText(etCreditScore)));

        RetrofitClient.getInstance().getApi().analyzeBudget(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayBudgetResult(response.body().getAsJsonObject("data"));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBudgetResult(JsonObject data) {
        resultSection.setVisibility(View.VISIBLE);

        double score = data.get("health_score").getAsDouble();
        String grade = data.get("grade").getAsString();
        JsonObject metrics = data.getAsJsonObject("metrics");

        tvHealthScore.setText(String.format("%.0f", score));
        tvGrade.setText("Grade: " + grade);
        tvSavingsRate.setText(String.format("%.1f%%", metrics.get("savings_rate").getAsDouble()));
        tvDti.setText(String.format("%.1f%%", metrics.get("debt_to_income_ratio").getAsDouble()));
        tvMonthlySavings.setText(String.format("$%.0f", metrics.get("monthly_savings").getAsDouble()));
    }

    private void calculateEmi() {
        String loanStr = getText(etEmiLoan);
        String rateStr = getText(etEmiRate);
        String termStr = getText(etEmiTerm);

        if (loanStr.isEmpty() || rateStr.isEmpty() || termStr.isEmpty()) {
            Toast.makeText(getContext(), "Fill all EMI fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("loan_amount", Double.parseDouble(loanStr));
        body.put("interest_rate", Double.parseDouble(rateStr));
        body.put("loan_term", Integer.parseInt(termStr));

        RetrofitClient.getInstance().getApi().calculateEMI(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayEmiResult(response.body().getAsJsonObject("data"));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayEmiResult(JsonObject data) {
        emiResultSection.setVisibility(View.VISIBLE);
        tvEmi.setText(String.format("$%.2f", data.get("monthly_emi").getAsDouble()));
        tvTotalPayment.setText(String.format("$%.2f", data.get("total_payment").getAsDouble()));
        tvTotalInterest.setText(String.format("$%.2f", data.get("total_interest").getAsDouble()));
        boolean affordable = data.get("affordable").getAsBoolean();
        tvAffordable.setText(affordable ? "✅ Affordable" : "⚠️ May be difficult");
        tvAffordable.setTextColor(getResources().getColor(affordable ? R.color.success : R.color.warning, null));
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
