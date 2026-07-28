# Real-Time Chat Service

This is a backend REST API and WebSocket application for one-to-one direct messaging.

## Features
- **User Management**: Create and list users.
- **Conversations**: Start conversations and list a user's conversations.
- **Messaging (REST & WebSocket)**: Send messages via REST API or STOMP over WebSockets.
- **Stable Pagination**: Fetch message history with cursor-based pagination (no skipped or duplicated messages).
- **Authorization**: Users can only fetch messages for conversations they are a part of.

## Requirements
- Java 17+
- Maven (included wrapper `mvnw.cmd` / `./mvnw`)

## Running the Application
The application uses an in-memory H2 database by default, so no external database setup is required.

Run the Spring Boot app using the Maven wrapper:
```bash
./mvnw spring-boot:run
```
*(On Windows, use `mvnw.cmd spring-boot:run`)*

The server will start on `http://localhost:8080`.

## Running the Tests
Automated integration tests cover sending messages, cursor-based pagination, and authorization checks.
```bash
./mvnw test
```

## API Overview
Authentication is handled simply via an `X-User-Id` header for the purposes of this exercise.

### Users
- `POST /api/users` - Create a user
- `GET /api/users` - List all users

### Conversations
- `POST /api/conversations` - Get or create a conversation with a specific user
- `GET /api/conversations` - List all conversations for the authenticated user

### Messages
- `POST /api/conversations/{id}/messages` - Send a message in a conversation
- `GET /api/conversations/{id}/messages?cursor={messageId}&limit={20}` - Fetch message history

### WebSockets
- Connect to `/ws`
- Subscribe to `/topic/conversation/{conversationId}` for real-time messages.
- Send to `/app/chat.send`
