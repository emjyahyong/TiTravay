package com.titravay.dto;

import java.time.LocalDateTime;

/**
 * Représentation d'un message envoyée au client (WebSocket broadcast + REST historique).
 * Record immuable — pas de setters nécessaires côté sortie.
 */
public record MessageResponse(
        Long id,
        Long conversationId,
        String senderUsername,
        String content,
        LocalDateTime timestamp,
        LocalDateTime readAt
) {}
