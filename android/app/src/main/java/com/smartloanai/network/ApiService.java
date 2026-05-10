package com.smartloanai.network;

import com.smartloanai.model.*;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Retrofit API interface defining all backend endpoints.
 */
public interface ApiService {

    // ==================== AUTH ====================

    @POST("api/auth/signup")
    Call<JsonObject> signup(@Body Map<String, Object> body);

    @POST("api/auth/login")
    Call<JsonObject> login(@Body Map<String, Object> body);

    @GET("api/auth/profile")
    Call<JsonObject> getProfile();

    @PUT("api/auth/profile")
    Call<JsonObject> updateProfile(@Body Map<String, Object> body);

    // ==================== LOAN ====================

    @POST("api/loan/predict")
    Call<JsonObject> predictLoan(@Body Map<String, Object> body);

    @GET("api/loan/history")
    Call<JsonObject> getLoanHistory();

    @GET("api/loan/suggestions")
    Call<JsonObject> getSuggestions();

    // ==================== CHATBOT ====================

    @POST("api/chatbot/message")
    Call<JsonObject> sendChatMessage(@Body Map<String, String> body);

    @GET("api/chatbot/history")
    Call<JsonObject> getChatHistory();

    @DELETE("api/chatbot/history")
    Call<JsonObject> clearChatHistory();

    // ==================== BUDGET ====================

    @POST("api/budget/analyze")
    Call<JsonObject> analyzeBudget(@Body Map<String, Object> body);

    @POST("api/budget/emi-calculator")
    Call<JsonObject> calculateEMI(@Body Map<String, Object> body);

    // ==================== RECOMMENDATIONS ====================

    @POST("api/recommend/get")
    Call<JsonObject> getRecommendations(@Body Map<String, Object> body);

    @POST("api/recommend/feedback")
    Call<JsonObject> submitFeedback(@Body Map<String, Object> body);

    // ==================== ADMIN ====================

    @GET("api/admin/dashboard")
    Call<JsonObject> getAdminDashboard();

    @GET("api/admin/users")
    Call<JsonObject> getAdminUsers();

    @GET("api/admin/analytics")
    Call<JsonObject> getAdminAnalytics();

    @GET("api/admin/chatbot-logs")
    Call<JsonObject> getAdminChatLogs();

    @GET("api/admin/model-stats")
    Call<JsonObject> getModelStats();

    // ==================== EDA ====================

    @GET("api/eda/reports")
    Call<JsonObject> getEDAReports();

    @GET("api/eda/statistics")
    Call<JsonObject> getEDAStatistics();

    // ==================== HEALTH ====================

    @GET("api/health")
    Call<JsonObject> healthCheck();
}
