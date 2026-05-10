package com.titravay.controller;

import com.titravay.dto.ConversationStartResponse;
import com.titravay.dto.MessageResponse;
import com.titravay.service.ConversationService;
import com.titravay.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService      messageService;

    public ConversationController(ConversationService conversationService,
                                  MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService      = messageService;
    }

    /**
     * Crée ou retrouve la conversation liée à un service.
     * Retourne uniquement l'id — le frontend redirige vers /conversations/{id}.
     */
    @PostMapping("/service/{serviceId}")
    public ConversationStartResponse startOrGet(@PathVariable Long serviceId,
                                                Principal principal) {
        return conversationService.getOrCreate(serviceId, principal);
    }

    /**
     * Historique paginé des messages d'une conversation.
     * Vérifie que l'appelant est participant (403 sinon).
     */
    @GetMapping("/{id}/messages")
    public Page<MessageResponse> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            Principal principal) {
        return messageService.getMessages(id, principal, PageRequest.of(page, size));
    }

    /**
     * Marque comme lus tous les messages non lus de la conversation
     * envoyés par l'autre participant.
     */
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id, Principal principal) {
        messageService.markAsRead(id, principal);
    }
}
