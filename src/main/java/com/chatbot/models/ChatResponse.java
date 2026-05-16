package com.chatbot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ChatResponse — POJO representing the chatbot API response body.
 * Used to deserialise JSON and enable strongly-typed assertions.
 *
 * Example JSON:
 * {
 *   "response":  "Hello! How can I help you today?",
 *   "sessionId": "abc-123",
 *   "timestamp": "2025-09-01T12:34:56Z",
 *   "status":    "success",
 *   "confidence": 0.97,
 *   "intent":    "greeting",
 *   "metadata":  { "responseTimeMs": 423, "modelVersion": "v2.1", "tokensUsed": 38 }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    @JsonProperty("response")
    private String response;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("intent")
    private String intent;

    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    // ── Nested Metadata ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("responseTimeMs") private Long responseTimeMs;
        @JsonProperty("modelVersion")   private String modelVersion;
        @JsonProperty("tokensUsed")     private Integer tokensUsed;

        public Long   getResponseTimeMs() { return responseTimeMs; }
        public String getModelVersion()   { return modelVersion; }
        public Integer getTokensUsed()    { return tokensUsed; }

        @Override
        public String toString() {
            return String.format("Metadata{responseTimeMs=%d, modelVersion='%s', tokensUsed=%d}",
                responseTimeMs, modelVersion, tokensUsed);
        }
    }

    // ── Convenience Methods ───────────────────────────────────────────────────

    /** True if response status is "success". */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    /** True if the response text contains the given keyword (case-insensitive). */
    public boolean responseContains(String keyword) {
        return response != null && response.toLowerCase().contains(keyword.toLowerCase());
    }

    /** True if the response text is not null and not blank. */
    public boolean hasResponse() {
        return response != null && !response.trim().isEmpty();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String    getResponse()     { return response; }
    public String    getSessionId()    { return sessionId; }
    public String    getTimestamp()    { return timestamp; }
    public String    getStatus()       { return status; }
    public Double    getConfidence()   { return confidence; }
    public String    getIntent()       { return intent; }
    public Metadata  getMetadata()     { return metadata; }
    public String    getErrorCode()    { return errorCode; }
    public String    getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        return String.format(
            "ChatResponse{response='%s', sessionId='%s', status='%s', intent='%s', confidence=%.2f}",
            response, sessionId, status, intent, confidence != null ? confidence : 0.0);
    }
}
