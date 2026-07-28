package com.nitesh.chatservice.dto;

import com.nitesh.chatservice.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request payload for sending a new message in a conversation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
}
