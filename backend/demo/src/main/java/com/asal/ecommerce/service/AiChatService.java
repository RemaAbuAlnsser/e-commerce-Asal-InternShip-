package com.asal.ecommerce.service;

import com.asal.ecommerce.model.*;
import com.asal.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final DeliveryCityRepository deliveryCityRepository;
    private final SettingsRepository settingsRepository;

    @Value("${groq.api-key}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.1-8b-instant";

    public String chat(String userMessage) {
        return callGroqApi(buildSystemPrompt(), userMessage);
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        settingsRepository.findAll().stream().findFirst().ifPresent(s -> {
            sb.append("=== STORE INFORMATION ===\n");
            if (s.getSiteName()        != null) sb.append("Store Name: ").append(s.getSiteName()).append("\n");
            if (s.getSiteDescription() != null) sb.append("Description: ").append(s.getSiteDescription()).append("\n");
            if (s.getContactEmail()    != null) sb.append("Email: ").append(s.getContactEmail()).append("\n");
            if (s.getContactPhone()    != null) sb.append("Phone: ").append(s.getContactPhone()).append("\n");
            if (s.getAddress()         != null) sb.append("Address: ").append(s.getAddress()).append("\n");
            sb.append("\n");
        });

        List<Category> categories = categoryRepository.findAll();
        if (!categories.isEmpty()) {
            sb.append("=== CATEGORIES ===\n");
            categories.forEach(c -> sb.append("- ").append(c.getName())
                    .append(c.getDescription() != null ? ": " + c.getDescription() : "").append("\n"));
            sb.append("\n");
        }

        List<Brand> brands = brandRepository.findAll();
        if (!brands.isEmpty()) {
            sb.append("=== BRANDS ===\n");
            brands.forEach(b -> sb.append("- ").append(b.getName()).append("\n"));
            sb.append("\n");
        }

        List<DeliveryCity> cities = deliveryCityRepository.findAll();
        if (!cities.isEmpty()) {
            sb.append("=== DELIVERY CITIES ===\n");
            cities.forEach(c -> sb.append("- ").append(c.getCityName())
                    .append(" (fee: $").append(c.getDeliveryPrice()).append(")\n"));
            sb.append("\n");
        }

        List<Product> products = productRepository.findAll(PageRequest.of(0, 100)).getContent()
                .stream().filter(p -> "active".equals(p.getStatus())).collect(Collectors.toList());

        if (!products.isEmpty()) {
            sb.append("=== PRODUCTS ===\n");
            products.forEach(p -> {
                sb.append("- ").append(p.getName());
                sb.append(" | Price: $").append(p.getPrice());
                if (p.getOldPrice() != null) sb.append(" (was $").append(p.getOldPrice()).append(")");
                if (p.getCategory() != null) sb.append(" | Category: ").append(p.getCategory().getName());
                if (p.getBrand()    != null) sb.append(" | Brand: ").append(p.getBrand().getName());
                sb.append(" | Stock: ").append(p.getTotalStock() > 0 ? "In Stock (" + p.getTotalStock() + ")" : "Sold Out");
                if (p.isFeatured())  sb.append(" | Featured");
                if (p.isExclusive()) sb.append(" | Exclusive");
                if (p.getDescription() != null && !p.getDescription().isBlank())
                    sb.append(" | ").append(p.getDescription().replaceAll("\\s+", " ")
                            .substring(0, Math.min(120, p.getDescription().length())));
                sb.append("\n");
            });
            sb.append("\n");
        }

        sb.append("""
                === YOUR ROLE ===
                You are a helpful, friendly shopping assistant for this online store.
                Answer customer questions about products, prices, availability, categories, brands, and delivery.
                Be concise and helpful. If something isn't in the data above, say you don't have that information.
                Do not make up prices or product details.
                """);

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String callGroqApi(String systemPrompt, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "system",  "content", systemPrompt),
                        Map.of("role", "user",    "content", userMessage)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Sorry, I couldn't process your request right now.";
        } catch (HttpClientErrorException e) {
            log.error("Groq API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Sorry, I'm having trouble connecting right now. Please try again later.";
        } catch (Exception e) {
            log.error("Unexpected error calling Groq API: {}", e.getMessage(), e);
            return "Sorry, I'm having trouble connecting right now. Please try again later.";
        }
    }
}
