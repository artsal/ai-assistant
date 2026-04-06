package com.github.artsal.ai.assistant.backend.chat;

import com.github.artsal.ai.assistant.backend.model.Message;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/stream")
    public Flux<String> stream(
            @RequestBody List<Message> messages,
            @RequestParam(defaultValue = "chat") String mode) {

        return chatService.stream(messages, mode);
    }
}