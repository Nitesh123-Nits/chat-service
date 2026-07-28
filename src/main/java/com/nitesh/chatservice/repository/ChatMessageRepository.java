package com.nitesh.chatservice.repository;

import com.nitesh.chatservice.entity.ChatMessage;
import com.nitesh.chatservice.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatMessage entity.
 * Provides message history retrieval and bulk status updates.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Get the latest messages in a conversation, ordered descending (newest first).
     */
    List<ChatMessage> findByConversationIdOrderByIdDesc(Long conversationId, org.springframework.data.domain.Pageable pageable);

    /**
     * Get messages in a conversation older than the given cursor ID, ordered descending.
     */
    List<ChatMessage> findByConversationIdAndIdLessThanOrderByIdDesc(Long conversationId, Long id, org.springframework.data.domain.Pageable pageable);

    /**
     * Legacy method: Get all messages in a conversation, ordered chronologically.
     */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Count unread messages in a conversation for a specific user.
     * Unread = messages NOT sent by the user that are not yet READ.
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id <> :userId AND m.status <> 'READ'")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Mark all messages in a conversation as READ for a specific user.
     * Only updates messages that were NOT sent by the user.
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = :status WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id <> :userId AND m.status <> :status")
    int markMessagesAsRead(@Param("conversationId") Long conversationId,
                           @Param("userId") Long userId,
                           @Param("status") MessageStatus status);
}
