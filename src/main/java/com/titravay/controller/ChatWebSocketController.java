package com.titravay.controller;

import com.titravay.dto.MessageResponse;
import com.titravay.dto.SendMessageRequest;
import com.titravay.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Reçoit un message STOMP sur /app/chat/{conversationId}.
     * Vérifie l'authentification et la participation avant persistance,
     * puis broadcast le résultat sur /topic/conversations/{conversationId}.
     */
    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}")
    public MessageResponse sendMessage(
            @DestinationVariable Long conversationId,
            @Payload @Valid SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("Principal STOMP absent — connexion non authentifiée");
        }

        return messageService.sendMessage(conversationId, request, principal);
    }

    /**
     * Capture toute exception levée dans les handlers @MessageMapping
     * et renvoie le message d'erreur à l'utilisateur concerné uniquement
     * via /user/queue/errors.
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        log.warn("Erreur WebSocket interceptée : {}", ex.getMessage());
        return ex.getMessage();
    }
}
