package com.chatbot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ApiErrorResponse — POJO for API error responses (4xx / 5xx).
 *
 * Example JSON:
 * {
 *   "error":   "VALIDATION_ERROR",
 *   "message": "The 'message' field is required.",
 *   "code":    400,
 *   "path":    "/v1/chat",
 *   "timestamp": "2025-09-01T12:00:00Z"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiErrorResponse {

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("path")
    private String path;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("details")
    private java.util.List<String> details;

    // ── Convenience ───────────────────────────────────────────────────────────

    public boolean hasError()           { return error != null && !error.isEmpty(); }
    public boolean isClientError()      { return code != null && code >= 400 && code < 500; }
    public boolean isServerError()      { return code != null && code >= 500; }
    public boolean messageContains(String kw) {
        return message != null && message.toLowerCase().contains(kw.toLowerCase());
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String              getError()     { return error; }
    public String              getMessage()   { return message; }
    public Integer             getCode()      { return code; }
    public String              getPath()      { return path; }
    public String              getTimestamp() { return timestamp; }
    public java.util.List<String> getDetails(){ return details; }

    @Override
    public String toString() {
        return String.format("ApiErrorResponse{code=%d, error='%s', message='%s'}",
            code, error, message);
    }
}
