package com.smartloanai.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartloanai.R;
import com.smartloanai.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin dashboard activity showing app statistics.
 */
public class AdminActivity extends AppCompatActivity {

    private TextView tvUsers, tvPredictions, tvApprovalRate, tvChats;
    private RecyclerView rvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvUsers = findViewById(R.id.tv_total_users);
        tvPredictions = findViewById(R.id.tv_total_predictions);
        tvApprovalRate = findViewById(R.id.tv_approval_rate);
        tvChats = findViewById(R.id.tv_total_chats);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        fetchDashboardStats();
    }

    private void fetchDashboardStats() {
        RetrofitClient.getInstance().getApi().getAdminDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    
                    tvUsers.setText(data.get("total_users").getAsString());
                    tvPredictions.setText(data.get("total_predictions").getAsString());
                    tvApprovalRate.setText(data.get("approval_rate").getAsString() + "%");
                    tvChats.setText(data.get("total_chats").getAsString());
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
