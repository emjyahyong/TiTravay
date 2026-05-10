package com.titravay.dto;

/**
 * Réponse minimale à POST /api/conversations/service/{id}.
 * Expose uniquement l'id de la conversation pour la redirection frontend.
 */
public record ConversationStartResponse(Long id) {}
