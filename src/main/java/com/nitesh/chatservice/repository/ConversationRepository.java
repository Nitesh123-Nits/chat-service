package com.nitesh.chatservice.repository;

import com.nitesh.chatservice.entity.Conversation;
import com.nitesh.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Conversation entity.
 * Provides lookups for finding conversations between specific users.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find all conversations where the given user is a participant.
     */
    @Query("SELECT c FROM Conversation c WHERE c.participant1 = :user OR c.participant2 = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByParticipant(@Param("user") User user);

    /**
     * Find an existing conversation between two specific users (in either order).
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1 = :user1 AND c.participant2 = :user2) OR " +
           "(c.participant1 = :user2 AND c.participant2 = :user1)")
    Optional<Conversation> findByParticipants(@Param("user1") User user1, @Param("user2") User user2);
}
