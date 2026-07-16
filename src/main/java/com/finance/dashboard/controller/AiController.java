package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.AiChatRequest;
import com.finance.dashboard.dto.request.AiCategorizeRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI-powered endpoints using Claude (Anthropic).
 *
 * All require authentication. No role restriction —
 * every user can benefit from AI insights on their own data.
 *
 * Endpoints:
 *   GET  /api/ai/insights       — financial health analysis
 *   POST /api/ai/chat           — Q&A with finance context
 *   POST /api/ai/categorize     — suggest category for a transaction
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * GET /api/ai/insights
     *
     * No request body needed — service pulls live data from DB.
     * Returns a plain-text financial health analysis.
     */
    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<Map<String, String>>> insights() {
        String analysis = aiService.insights();
        return ResponseEntity.ok(ApiResponse.success(Map.of("analysis", analysis)));
    }

    /**
     * POST /api/ai/chat
     *
     * Body: { "message": "...", "history": [{role, content}, ...] }
     * Returns Claude's reply with live financial context injected.
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(
            @Valid @RequestBody AiChatRequest request) {
        String reply = aiService.chat(request.getMessage(), request.getHistory());
        return ResponseEntity.ok(ApiResponse.success(Map.of("reply", reply)));
    }

    /**
     * POST /api/ai/categorize
     *
     * Body: { "description": "Amazon groceries", "type": "EXPENSE" }
     * Returns a single category name suggestion.
     */
    @PostMapping("/categorize")
    public ResponseEntity<ApiResponse<Map<String, String>>> categorize(
            @Valid @RequestBody AiCategorizeRequest request) {
        String category = aiService.categorize(request.getDescription(), request.getType());
        return ResponseEntity.ok(ApiResponse.success(Map.of("category", category)));
    }
}
