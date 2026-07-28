package com.nitesh.chatservice.controller;

import com.nitesh.chatservice.dto.ConversationRequest;
import com.nitesh.chatservice.dto.ConversationResponse;
import com.nitesh.chatservice.dto.MessageRequest;
import com.nitesh.chatservice.dto.MessageResponse;
import com.nitesh.chatservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ConversationResponse> getOrCreateConversation(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ConversationRequest request) {
        
        ConversationResponse response = chatService.getOrCreateConversation(userId, request.getParticipantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            @RequestHeader("X-User-Id") Long userId) {
        
        List<ConversationResponse> responses = chatService.getUserConversations(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<MessageResponse> responses = chatService.getMessages(conversationId, userId, cursor, limit);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequest request) {
        
        MessageResponse response = chatService.sendMessage(conversationId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId) {
        
        chatService.markAsRead(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
}
