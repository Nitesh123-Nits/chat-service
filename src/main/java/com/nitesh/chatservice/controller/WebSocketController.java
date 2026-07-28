package com.nitesh.chatservice.controller;

import com.nitesh.chatservice.dto.MessageRequest;
import com.nitesh.chatservice.dto.MessageResponse;
import com.nitesh.chatservice.dto.WebSocketMessage;
import com.nitesh.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles incoming STOMP messages, saves them via ChatService, 
     * and broadcasts the response to the conversation topic.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload WebSocketMessage webSocketMessage) {
        // Action SEND means it's a real message to save
        if ("SEND".equalsIgnoreCase(webSocketMessage.getAction())) {
            MessageRequest messageRequest = MessageRequest.builder()
                    .content(webSocketMessage.getContent())
                    .messageType(webSocketMessage.getMessageType())
                    .build();

            MessageResponse response = chatService.sendMessage(
                    webSocketMessage.getConversationId(),
                    webSocketMessage.getSenderId(),
                    messageRequest
            );

            // Broadcast the saved message to subscribers
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + webSocketMessage.getConversationId(),
                    response
            );
        } else if ("TYPING".equalsIgnoreCase(webSocketMessage.getAction()) || 
                   "STOP_TYPING".equalsIgnoreCase(webSocketMessage.getAction())) {
            
            // For typing indicators, just broadcast the event directly
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + webSocketMessage.getConversationId() + "/typing",
                    webSocketMessage
            );
        }
    }
}
