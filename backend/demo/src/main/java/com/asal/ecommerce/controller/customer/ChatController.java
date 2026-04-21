package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.ChatRequest;
import com.asal.ecommerce.dto.ChatResponse;
import com.asal.ecommerce.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = aiChatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
