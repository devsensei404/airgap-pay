# airgap-pay

Payment settlement system for environments with no internet connectivity - hybrid RSA+AES-GCM encryption, idempotent processing, and replay protection for transactions that reach the backend through untrusted intermediary devices.

## Status
Work in progress... building this to understand distributed-systems security concepts (hybrid encryption, idempotency, replay protection) hands-on.

## Stack
- Java 21, Spring Boot 4.1.1
- Spring Web, Spring Data JPA, H2 (in-memory)
- Plain HTML/JS frontend (no framework)

## Concepts covered
- Hybrid encryption (RSA-OAEP + AES-GCM)
- Idempotent settlement (atomic claim via `putIfAbsent`)
- Replay attack prevention (TTL + nonce)

More details coming as the build progresses.