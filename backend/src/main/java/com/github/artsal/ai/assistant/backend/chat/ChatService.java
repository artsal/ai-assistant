package com.github.artsal.ai.assistant.backend.chat;

import com.github.artsal.ai.assistant.backend.model.Message;
import com.github.artsal.ai.assistant.backend.rag.DocumentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final DocumentService documentService;

    public ChatService(ChatClient.Builder builder, DocumentService documentService) {
        this.chatClient = builder.build();
        this.documentService = documentService;
    }

    public Flux<String> stream(List<Message> messages, String mode) {

        StringBuilder conversation = new StringBuilder();

        if ("docs".equalsIgnoreCase(mode)) {

            String lastUserMessage = messages.get(messages.size() - 1).getContent();

            String relevantChunk = documentService.getRelevantChunk(lastUserMessage);

            if (relevantChunk != null && !relevantChunk.isEmpty()) {

                conversation.append("""
                                You are an assistant that answers ONLY from the provided context.

                                If the answer is not in the context, say:
                                "I don't know based on the provided document."

                                Context:
                                """)
                        .append(relevantChunk)
                        .append("\n\n");
            }
        }

        for (Message msg : messages) {
            if ("user".equalsIgnoreCase(msg.getRole())) {
                conversation.append("User: ").append(msg.getContent()).append("\n");
            } else {
                conversation.append("AI: ").append(msg.getContent()).append("\n");
            }
        }

        conversation.append("AI:");

        return chatClient.prompt()
                .user(conversation.toString())
                .stream()
                .content();
    }
}