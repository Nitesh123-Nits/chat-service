package com.nitesh.chatservice.service;

import com.nitesh.chatservice.dto.UserRequest;
import com.nitesh.chatservice.dto.UserResponse;
import com.nitesh.chatservice.entity.User;
import com.nitesh.chatservice.entity.UserStatus;
import com.nitesh.chatservice.exception.BadRequestException;
import com.nitesh.chatservice.exception.ResourceNotFoundException;
import com.nitesh.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .status(UserStatus.ONLINE) // default to online when created
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    public UserResponse getUser(Long id) {
        User user = getUserEntity(id);
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserStatus(Long id, UserStatus status) {
        User user = getUserEntity(id);
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * Internal method to fetch User entity or throw if not found.
     */
    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
