package com.finance.dashboard.service;

import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Calls Anthropic Claude API for three AI features:
 *   1. insights()     — financial health analysis from transaction summary
 *   2. chat()         — free-form Q&A about the user's own data
 *   3. categorize()   — suggest a transaction category from description
 *
 * Uses plain RestTemplate — no Spring AI dependency needed.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String CLAUDE_URL    = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL  = "claude-sonnet-4-6";
    private static final String ANTHROPIC_VER = "2023-06-01";

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // ── 1. Financial Insights ─────────────────────────────────────────────────

    /**
     * Builds a transaction summary from DB and asks Claude for
     * financial health analysis + actionable recommendations.
     */
    public String insights() {
        BigDecimal income   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal expenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal net      = income.subtract(expenses);

        List<Object[]> catRows = transactionRepository.sumByCategory();
        StringBuilder catSummary = new StringBuilder();
        catRows.forEach(row ->
            catSummary.append("  - ").append(row[0]).append(" (").append(row[1]).append("): ₹").append(row[2]).append("\n")
        );

        String prompt = """
                You are a personal finance advisor. Analyze this financial summary and give \
                3-5 specific, actionable insights. Be concise and direct.

                FINANCIAL SUMMARY:
                Total Income:   ₹%s
                Total Expenses: ₹%s
                Net Balance:    ₹%s

                Category Breakdown:
                %s

                Provide:
                1. Overall financial health assessment (1 sentence)
                2. Top 2-3 specific observations about spending patterns
                3. 2-3 actionable recommendations
                4. One key metric to watch

                Keep response under 300 words. Use plain text, no markdown headers.
                """.formatted(income, expenses, net, catSummary);

        return callClaude(prompt, 600);
    }

    // ── 2. AI Chat ────────────────────────────────────────────────────────────

    /**
     * Answers user's finance question with live DB context injected.
     * history = [{role:"user",content:"..."},{role:"assistant",content:"..."},...]
     */
    public String chat(String userMessage, List<Map<String, String>> history) {
        // Inject current financial context into system prompt
        BigDecimal income   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal expenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal net      = income.subtract(expenses);
        LocalDate since     = LocalDate.now().minusMonths(3);

        List<Object[]> recent = transactionRepository.monthlyTrends(since);
        StringBuilder trendStr = new StringBuilder();
        recent.forEach(row ->
            trendStr.append(row[0]).append("-").append(row[1])
                    .append(" ").append(row[2]).append(": ₹").append(row[3]).append("\n")
        );

        String system = """
                You are a helpful financial assistant for FinanceDash, a personal finance app.
                
                CURRENT USER DATA:
                Total Income:   ₹%s
                Total Expenses: ₹%s
                Net Balance:    ₹%s
                
                Recent 3-month trends:
                %s
                
                Answer questions about this data concisely. If asked about something outside \
                this data scope, say so honestly. Never invent numbers not in the data above.
                Keep answers under 150 words.
                """.formatted(income, expenses, net, trendStr);

        return callClaudeWithHistory(system, userMessage, history, 400);
    }

    // ── 3. Auto-categorize ────────────────────────────────────────────────────

    /**
     * Given a transaction description (notes/category hint),
     * returns a suggested category name.
     * Returns one of the standard categories or a sensible new one.
     */
    public String categorize(String description, String type) {
        String prompt = """
                Given this transaction description, suggest the most appropriate single category name.
                
                Transaction type: %s
                Description: "%s"
                
                Common categories for INCOME: Salary, Freelance, Investment, Business, Bonus, Rental, Other Income
                Common categories for EXPENSE: Rent, Groceries, Utilities, Transport, Healthcare, Education, Shopping, Entertainment, Travel, Food, Insurance, EMI, Other Expense
                
                Reply with ONLY the category name — one to three words, nothing else.
                If description is empty or unclear, reply: General
                """.formatted(type, description);

        return callClaude(prompt, 20).trim()
                .replaceAll("[\"'\\n]", "")
                .trim();
    }

    // ── Anthropic API helpers ─────────────────────────────────────────────────

    private String callClaude(String userPrompt, int maxTokens) {
        return callClaudeWithHistory(null, userPrompt, Collections.emptyList(), maxTokens);
    }

    @SuppressWarnings("unchecked")
    private String callClaudeWithHistory(String system,
                                         String userMessage,
                                         List<Map<String, String>> history,
                                         int maxTokens) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VER);

            // Build messages array: history + new user message
            List<Map<String, Object>> messages = new ArrayList<>();
            if (history != null) {
                history.forEach(h -> messages.add(Map.of(
                        "role",    h.getOrDefault("role", "user"),
                        "content", h.getOrDefault("content", "")
                )));
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model",      CLAUDE_MODEL);
            body.put("max_tokens", maxTokens);
            if (system != null && !system.isBlank()) {
                body.put("system", system);
            }
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(CLAUDE_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> content =
                        (List<Map<String, Object>>) response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }
            return "AI response unavailable. Please try again.";

        } catch (Exception e) {
            log.error("Anthropic API call failed: {}", e.getMessage());
            return "AI service temporarily unavailable: " + e.getMessage();
        }
    }
}
