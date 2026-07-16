package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AiChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    /** Optional conversation history for multi-turn chat.
     *  Each entry: { "role": "user"|"assistant", "content": "..." }
     *  Max 10 turns to keep context window sane. */
    private List<Map<String, String>> history = Collections.emptyList();

    public String getMessage()                              { return message; }
    public void setMessage(String v)                        { this.message = v; }
    public List<Map<String, String>> getHistory()           { return history; }
    public void setHistory(List<Map<String, String>> v)     { this.history = v != null ? v : Collections.emptyList(); }
}
