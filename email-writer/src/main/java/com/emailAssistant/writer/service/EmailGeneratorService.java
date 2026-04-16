package com.emailAssistant.writer.service;

import com.emailAssistant.writer.model.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.Map;

@Service
public class EmailGeneratorService {

    private WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> generateEmailReply(EmailRequest req) {
        //build the prompt
        String prompt = buildPrompt(req);
        //craft a req
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", prompt)
                        })
                }
        );
        //do req and get resp
        return webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractResponseContent);
        //return resp;
    }

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


    private String buildPrompt(EmailRequest req) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line ");
        if(req.getTone() != null && !req.getTone().isEmpty()) {
            prompt.append("Use a ").append(req.getTone()).append(" tone");
        }
        prompt.append("\nOriginal email: \n").append(req.getEmailContent());
        return prompt.toString();
    }
}
