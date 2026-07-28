package com.nitesh.chatservice.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a conversation.
 * Includes participant info, timestamps, and unread message count.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    private Long id;
    private UserResponse participant1;
    private UserResponse participant2;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
