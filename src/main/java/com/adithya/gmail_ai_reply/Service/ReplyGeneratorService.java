package com.adithya.gmail_ai_reply.Service;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.adithya.gmail_ai_reply.DTO.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service for generating professional email replies using Google's Gemini API.
 * Builds a prompt from input, invokes the API, and extracts the response content.
 */
@Slf4j
@Service
public class ReplyGeneratorService {

    private final WebClient webClient;
    private final ApiKeyManager apiKeyManager;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public ReplyGeneratorService(WebClient.Builder webClientBuilder, ApiKeyManager apiKeyManager) {
        this.webClient = webClientBuilder.build();
        this.apiKeyManager = apiKeyManager;
    }

    public String generateReply(EmailRequest emailRequest) {

        //Build prompt
        String prompt = "Write a professional email reply based on the content below. " +
                "Do not include a subject line. " +
                "Ensure the response sounds natural and human-written — avoid any tone that feels AI-generated. " +
                "Return only the reply body, formatted so it can be directly copied and pasted.\n" +
                "Email content:\n" + emailRequest.getEmailContent();

        // Craft a request
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        int attempts = apiKeyManager.totalKeys();
        for (int i = 0; i < attempts; i++) {
            String apiKey = apiKeyManager.getNextKey();

            try {
                String response = webClient.post()
                        .uri(geminiApiUrl + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                // Extract Response and Return
                return extractResponseContent(response);
            }catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {
                    // Current key is exhausted → rotate
                    apiKeyManager.markKeyExhausted();
                    log.info("Api key rotated");
                    continue; // retry with next key
                }
                throw e;
            }
        }

        throw new RuntimeException("All API keys exhausted or failed.");
    }

    /**
     * @param response (JSON format)
     * @return String form of the reply
     */
    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error processing request: " + e.getMessage();
        }
    }
}