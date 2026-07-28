package com.nitesh.chatservice.service;

import com.nitesh.chatservice.dto.ConversationResponse;
import com.nitesh.chatservice.dto.MessageRequest;
import com.nitesh.chatservice.dto.MessageResponse;
import com.nitesh.chatservice.dto.UserResponse;
import com.nitesh.chatservice.entity.ChatMessage;
import com.nitesh.chatservice.entity.Conversation;
import com.nitesh.chatservice.entity.MessageStatus;
import com.nitesh.chatservice.entity.User;
import com.nitesh.chatservice.exception.BadRequestException;
import com.nitesh.chatservice.exception.ForbiddenException;
import com.nitesh.chatservice.exception.ResourceNotFoundException;
import com.nitesh.chatservice.repository.ChatMessageRepository;
import com.nitesh.chatservice.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    @Transactional
    public ConversationResponse getOrCreateConversation(Long userId1, Long userId2) {
        User user1 = userService.getUserEntity(userId1);
        User user2 = userService.getUserEntity(userId2);

        return conversationRepository.findByParticipants(user1, user2)
                .map(conv -> mapToResponse(conv, userId1))
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .participant1(user1)
                            .participant2(user2)
                            .build();
                    newConv = conversationRepository.save(newConv);
                    return mapToResponse(newConv, userId1);
                });
    }

    public List<ConversationResponse> getUserConversations(Long userId) {
        User user = userService.getUserEntity(userId);
        return conversationRepository.findByParticipant(user).stream()
                .map(conv -> mapToResponse(conv, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, MessageRequest request) {
        Conversation conversation = getConversationEntity(conversationId);
        User sender = userService.getUserEntity(senderId);

        // Verify sender is part of conversation
        if (!conversation.getParticipant1().getId().equals(senderId) &&
            !conversation.getParticipant2().getId().equals(senderId)) {
            throw new ForbiddenException("User is not a participant in this conversation");
        }

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .build();

        message = chatMessageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapToResponse(message);
    }

    public List<MessageResponse> getMessages(Long conversationId, Long userId, Long cursor, int limit) {
        Conversation conversation = getConversationEntity(conversationId);
        if (!conversation.getParticipant1().getId().equals(userId) &&
            !conversation.getParticipant2().getId().equals(userId)) {
             throw new ForbiddenException("User is not a participant in this conversation");
        }
        
        List<ChatMessage> messages;
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (cursor == null) {
            messages = chatMessageRepository.findByConversationIdOrderByIdDesc(conversationId, pageRequest);
        } else {
            messages = chatMessageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(conversationId, cursor, pageRequest);
        }
        
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        chatMessageRepository.markMessagesAsRead(conversationId, userId, MessageStatus.READ);
    }

    public Conversation getConversationEntity(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }

    private ConversationResponse mapToResponse(Conversation conv, Long requestingUserId) {
        long unreadCount = chatMessageRepository.countUnreadMessages(conv.getId(), requestingUserId);
        
        return ConversationResponse.builder()
                .id(conv.getId())
                .participant1(mapUserResponse(conv.getParticipant1()))
                .participant2(mapUserResponse(conv.getParticipant2()))
                .createdAt(conv.getCreatedAt())
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }

    private UserResponse mapUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private MessageResponse mapToResponse(ChatMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderDisplayName(message.getSender().getDisplayName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
