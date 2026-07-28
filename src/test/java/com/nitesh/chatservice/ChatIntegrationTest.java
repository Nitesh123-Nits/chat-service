package com.nitesh.chatservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitesh.chatservice.dto.ConversationRequest;
import com.nitesh.chatservice.dto.MessageRequest;
import com.nitesh.chatservice.dto.MessageResponse;
import com.nitesh.chatservice.dto.UserRequest;
import com.nitesh.chatservice.entity.Conversation;
import com.nitesh.chatservice.entity.User;
import com.nitesh.chatservice.repository.ChatMessageRepository;
import com.nitesh.chatservice.repository.ConversationRepository;
import com.nitesh.chatservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User user1;
    private User user2;
    private User user3;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        // Create 3 users
        user1 = userRepository.save(User.builder().username("alice").displayName("Alice").build());
        user2 = userRepository.save(User.builder().username("bob").displayName("Bob").build());
        user3 = userRepository.save(User.builder().username("charlie").displayName("Charlie").build());

        // Create conversation between user1 and user2
        conversation = conversationRepository.save(Conversation.builder()
                .participant1(user1)
                .participant2(user2)
                .build());
    }

    @Test
    void testSendMessage() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .content("Hello Bob!")
                .build();

        mockMvc.perform(post("/api/conversations/" + conversation.getId() + "/messages")
                        .header("X-User-Id", user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(chatMessageRepository.findAll()).hasSize(1);
    }

    @Test
    void testAuthorizationDeniedRead() throws Exception {
        // User 3 is not part of the conversation between user 1 and user 2
        mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages")
                        .header("X-User-Id", user3.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPaginatedFetchWithCursor() throws Exception {
        // Send 5 messages sequentially
        for (int i = 1; i <= 5; i++) {
            MessageRequest request = MessageRequest.builder()
                    .content("Message " + i)
                    .build();
            mockMvc.perform(post("/api/conversations/" + conversation.getId() + "/messages")
                            .header("X-User-Id", user1.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Fetch latest 2 messages (limit = 2)
        MvcResult latestResult = mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages")
                        .header("X-User-Id", user1.getId())
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andReturn();

        List<MessageResponse> latestMessages = objectMapper.readValue(latestResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(latestMessages).hasSize(2);
        
        // Since we order by ID desc, the latest messages should be Message 5 and Message 4
        assertThat(latestMessages.get(0).getContent()).isEqualTo("Message 5");
        assertThat(latestMessages.get(1).getContent()).isEqualTo("Message 4");

        // The cursor for the next page is the ID of the oldest message in the current page (Message 4)
        Long nextCursor = latestMessages.get(1).getId();

        // Fetch next 2 messages older than cursor
        MvcResult nextResult = mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages")
                        .header("X-User-Id", user1.getId())
                        .param("cursor", nextCursor.toString())
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andReturn();

        List<MessageResponse> nextMessages = objectMapper.readValue(nextResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(nextMessages).hasSize(2);
        
        // These should be Message 3 and Message 2
        assertThat(nextMessages.get(0).getContent()).isEqualTo("Message 3");
        assertThat(nextMessages.get(1).getContent()).isEqualTo("Message 2");
    }
}
