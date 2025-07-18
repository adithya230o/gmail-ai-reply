# gmail-ai-reply

This project connects a Chrome extension with a Spring Boot backend to generate professional AI replies for Gmail threads using Gemini Flash 2.0.

## ✦ Overview

- The **Chrome Extension** injects an “AI Reply” button inside Gmail's reply toolbar (not visible in new email compose windows).
- When clicked, it:
  - Detects the correct compose box,
  - Extracts the email content,
  - Sends a request to the backend,
  - Pastes the AI-generated reply into the reply box,
  - Displays time taken to generate the response (handled in `content.js`).
- UI manipulation is handled using DOM traversal and `MutationObserver`.

## ✦ Backend (Spring Boot)

- Provides a `/generate` endpoint to serve AI-generated replies.
- Integrates Gemini Flash 2.0 API.
- Enforces strict rate limiting:
  - `14 requests / minute`
  - `199 requests / day`
- Dockerized for easy deployment.

## ✦ Tech Stack

- Java 17, Spring Boot
- Gemini Flash 2.0 API
- Chrome Extension (Manifest V3 + JavaScript)
- Docker

## ✦ Hosting

Backend is containerized and deployable to any Docker-compatible environment. No persistent storage is used.

## ✦ Security & Privacy

- No user data or email content is stored.
- All processing is stateless and in-memory.
- This project is not affiliated with Google or Gemini.

---

