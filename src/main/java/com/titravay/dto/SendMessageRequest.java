package com.titravay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload entrant (WebSocket STOMP /app/chat/{id}). */
public class SendMessageRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 2000, message = "Le message est limité à 2000 caractères")
    private String content;

    public SendMessageRequest() {}

    public String getContent()             { return content; }
    public void setContent(String content) { this.content = content; }
}
