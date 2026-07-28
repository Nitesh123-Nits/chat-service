package com.nitesh.chatservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload for creating or retrieving a conversation between two users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequest {

    @NotNull(message = "The other participant's user ID is required")
    private Long participantId;
}
