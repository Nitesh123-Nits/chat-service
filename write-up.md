# Take-Home Exercise Write-Up

### 1. What did you ask the AI to do, and what did you write or decide yourself?
**AI:** I provided the AI with the project scaffold and the general requirement to build a "Real-Time Chat Service with REST and WebSockets." I later provided the specific PDF requirements. The AI generated the domain entities, JPA repositories, service logic, controllers, DTOs, and the integration test class.
**Myself:** I decided the high-level architecture (Spring Boot 3 + H2 in-memory), directed the AI to follow a strict commit-by-commit Git strategy to keep history clean, and explicitly prompted the AI to switch from basic offset pagination to robust cursor-based pagination after reviewing the PDF requirements. I also decided to use `X-User-Id` header for simplified authentication.

### 2. Where did you override, correct, or throw away the AI’s output — and why?
The AI initially planned to use standard offset-based pagination for fetching message history (`page=0, size=20`). I realized this would fail the PDF's requirement that "History pagination stays stable even as new messages arrive (no duplicates or skipped messages)." I overrode the AI's plan and forced it to rewrite the `ChatMessageRepository` and `ChatService` to use **cursor-based pagination** using the message `id` (ordering descending). This is much more stable for real-time chat histories.

### 3. The two or three biggest trade-offs you made, and the alternatives you considered.
1. **In-Memory H2 DB vs Postgres/MySQL:**
   * *Trade-off:* I used H2 to keep the project completely self-contained and easy to run instantly without Docker or DB setup. 
   * *Alternative:* PostgreSQL would be mandatory for production persistence, but for a short take-home assignment, zero-config startup time is often preferred by reviewers.
2. **X-User-Id Header vs Spring Security JWT:**
   * *Trade-off:* I implemented a simple `X-User-Id` header to simulate the authenticated user making requests. It's completely insecure but allows demonstrating the business logic (like Auth checks on conversation access).
   * *Alternative:* Implementing full Spring Security + JWT would take an extra hour and distract from the core messaging logic.
3. **Cursor Pagination on ID vs Timestamp:**
   * *Trade-off:* I used the auto-incrementing message `id` as the pagination cursor rather than `createdAt`. 
   * *Alternative:* Using a timestamp can cause edge cases if two messages are sent in the exact same millisecond, whereas an auto-incrementing ID or snowflake ID guarantees strict ordering.

### 4. What’s missing, or what you’d do with another day?
If I had another day, I would add:
1. **Real Authentication:** Swap the `X-User-Id` header for Spring Security with a JWT filter.
2. **Persistent Database:** Move to PostgreSQL using Testcontainers for the integration tests.
3. **Optimized Queries:** Add database indexing on `(conversation_id, id DESC)` to speed up the cursor pagination queries.
4. **WebSocket Security:** Currently, the WebSocket endpoint doesn't validate if the user subscribing to a topic is actually a participant in that conversation. I would add a `ChannelInterceptor` to secure STOMP subscriptions.
