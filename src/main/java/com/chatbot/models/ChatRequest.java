package com.chatbot.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ChatRequest — POJO representing the chatbot API request body.
 * Use with REST Assured instead of raw Maps for type safety.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {

    @JsonProperty("message")
    private String message;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("language")
    private String language;

    @JsonProperty("context")
    private String context;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ChatRequest() {}

    public ChatRequest(String message, String sessionId, String userId) {
        this.message   = message;
        this.sessionId = sessionId;
        this.userId    = userId;
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ChatRequest req = new ChatRequest();

        public Builder message(String message)     { req.message   = message;   return this; }
        public Builder sessionId(String sessionId) { req.sessionId = sessionId; return this; }
        public Builder userId(String userId)       { req.userId    = userId;    return this; }
        public Builder language(String language)   { req.language  = language;  return this; }
        public Builder context(String context)     { req.context   = context;   return this; }
        public ChatRequest build()                 { return req; }
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getMessage()   { return message; }
    public String getSessionId() { return sessionId; }
    public String getUserId()    { return userId; }
    public String getLanguage()  { return language; }
    public String getContext()   { return context; }

    public void setMessage(String message)     { this.message   = message; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setUserId(String userId)       { this.userId    = userId; }
    public void setLanguage(String language)   { this.language  = language; }
    public void setContext(String context)     { this.context   = context; }

    @Override
    public String toString() {
        return String.format("ChatRequest{message='%s', sessionId='%s', userId='%s'}",
            message, sessionId, userId);
    }
}
