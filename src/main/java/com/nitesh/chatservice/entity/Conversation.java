package com.nitesh.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a one-to-one conversation between two users.
 * A unique constraint ensures no duplicate conversations exist.
 */
@Entity
@Table(name = "conversations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"participant1_id", "participant2_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant1_id", nullable = false)
    private User participant1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant2_id", nullable = false)
    private User participant2;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime lastMessageAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastMessageAt == null) {
            lastMessageAt = now;
        }
    }
}
