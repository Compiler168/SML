package com.smartloanai.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.smartloanai.R;
import com.smartloanai.model.ChatMessage;
import com.smartloanai.network.RetrofitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AI Chatbot fragment with RecyclerView chat interface.
 */
public class ChatbotFragment extends Fragment {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        rvChat = view.findViewById(R.id.rv_chat);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);

        // Setup RecyclerView
        adapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        // Welcome message
        messages.add(ChatMessage.botMessage(
            "Hello! 👋 I'm your Smart Loan AI Assistant!\n\n" +
            "I can help you with:\n" +
            "💰 Loan eligibility & advice\n" +
            "📊 EMI calculations\n" +
            "💡 Budgeting & savings tips\n" +
            "📈 Credit score improvement\n\n" +
            "What would you like to know?"
        ));
        adapter.notifyItemInserted(messages.size() - 1);

        // Quick suggestion chips
        setupSuggestionChips(view);

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        return view;
    }

    private void setupSuggestionChips(View view) {
        View chip1 = view.findViewById(R.id.chip_loan);
        View chip2 = view.findViewById(R.id.chip_credit);
        View chip3 = view.findViewById(R.id.chip_budget);
        View chip4 = view.findViewById(R.id.chip_emi);

        if (chip1 != null) chip1.setOnClickListener(v -> sendQuickMessage("How can I improve my loan eligibility?"));
        if (chip2 != null) chip2.setOnClickListener(v -> sendQuickMessage("How to improve my credit score?"));
        if (chip3 != null) chip3.setOnClickListener(v -> sendQuickMessage("Give me budgeting tips"));
        if (chip4 != null) chip4.setOnClickListener(v -> sendQuickMessage("How is EMI calculated?"));
    }

    private void sendQuickMessage(String text) {
        etMessage.setText(text);
        sendMessage();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // Add user message
        messages.add(ChatMessage.userMessage(text));
        adapter.notifyItemInserted(messages.size() - 1);

        // Add loading indicator
        messages.add(ChatMessage.loadingMessage());
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        etMessage.setText("");
        btnSend.setEnabled(false);

        // API call
        Map<String, String> body = new HashMap<>();
        body.put("message", text);

        RetrofitClient.getInstance().getApi().sendChatMessage(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                // Remove loading
                removeLoading();
                btnSend.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    String botResponse = data.get("response").getAsString();
                    messages.add(ChatMessage.botMessage(botResponse));
                } else {
                    messages.add(ChatMessage.botMessage("Sorry, I couldn't process your request. Please try again."));
                }
                adapter.notifyItemInserted(messages.size() - 1);
                scrollToBottom();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                removeLoading();
                btnSend.setEnabled(true);
                messages.add(ChatMessage.botMessage("Connection error. Please check your internet connection."));
                adapter.notifyItemInserted(messages.size() - 1);
                scrollToBottom();
            }
        });
    }

    private void removeLoading() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).isLoading()) {
                messages.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void scrollToBottom() {
        rvChat.post(() -> rvChat.smoothScrollToPosition(Math.max(0, messages.size() - 1)));
    }

    /**
     * Chat RecyclerView adapter with user/bot bubble differentiation.
     */
    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private final List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).getType();
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == ChatMessage.TYPE_USER ?
                R.layout.item_chat_user : R.layout.item_chat_bot;
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.tvMessage.setText(msg.getContent());
            if (msg.isLoading()) {
                holder.tvMessage.setAlpha(0.6f);
            } else {
                holder.tvMessage.setAlpha(1.0f);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            ChatViewHolder(View view) {
                super(view);
                tvMessage = view.findViewById(R.id.tv_message);
            }
        }
    }
}
