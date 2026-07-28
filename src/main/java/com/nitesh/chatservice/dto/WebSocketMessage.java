package com.nitesh.chatservice.dto;

import com.nitesh.chatservice.entity.MessageType;
import lombok.*;

/**
 * Payload used for real-time WebSocket/STOMP message exchange.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private MessageType messageType;

    /**
     * Action type for the WebSocket message.
     * Examples: SEND, TYPING, STOP_TYPING
     */
    private String action;
}
