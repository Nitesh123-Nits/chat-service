package com.nitesh.chatservice.dto;

import com.nitesh.chatservice.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String displayName;
    private UserStatus status;
    private LocalDateTime createdAt;
}
