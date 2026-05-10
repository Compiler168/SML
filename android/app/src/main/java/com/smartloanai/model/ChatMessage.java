package com.smartloanai.model;

/**
 * Data model for chat messages.
 */
public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String content;
    private int type;
    private String timestamp;
    private boolean isLoading;

    public ChatMessage(String content, int type, String timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.isLoading = false;
    }

    public static ChatMessage userMessage(String content) {
        return new ChatMessage(content, TYPE_USER, String.valueOf(System.currentTimeMillis()));
    }

    public static ChatMessage botMessage(String content) {
        return new ChatMessage(content, TYPE_BOT, String.valueOf(System.currentTimeMillis()));
    }

    public static ChatMessage loadingMessage() {
        ChatMessage msg = new ChatMessage("Thinking...", TYPE_BOT, String.valueOf(System.currentTimeMillis()));
        msg.setLoading(true);
        return msg;
    }

    // Getters and setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { isLoading = loading; }
    public boolean isUser() { return type == TYPE_USER; }
}
