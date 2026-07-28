package com.nitesh.chatservice.dto;

import com.nitesh.chatservice.entity.MessageStatus;
import com.nitesh.chatservice.entity.MessageType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a single chat message.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private LocalDateTime createdAt;
}
