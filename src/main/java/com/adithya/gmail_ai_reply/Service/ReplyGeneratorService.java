package com.adithya.gmail_ai_reply.Service;

import com.adithya.gmail_ai_reply.DTO.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;


/**
 * Service for generating professional email replies using Google's Gemini API.
 * Builds a prompt from input, invokes the API, and extracts the response content.
 */

@Service
public class ReplyGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public ReplyGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public String generateReply(EmailRequest emailRequest) {

        //Build prompt
        String prompt = "Generate a professional email reply without a subject line. " +
                "DONT MAKE IT LOOK LIKE AI GENERATED. " +
                "Give me just the reply so that I can directly copy and paste it." +
                "\nEmail content : \n"
                + emailRequest.getEmailContent();


        // Craft a request
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );



        // Do request and get response
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();



        // Extract Response and Return
        return extractResponseContent(response);

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
