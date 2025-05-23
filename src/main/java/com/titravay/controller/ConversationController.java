package com.titravay.controller;

import com.titravay.model.Conversation;
import com.titravay.model.Services;
import com.titravay.model.User;
import com.titravay.repository.ServiceRepository;
import com.titravay.repository.UserRepository;
import com.titravay.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ServiceRepository serviceRepository;

    @PostMapping("/service/{serviceId}")
    public Conversation startOrGetConversation(@PathVariable Long serviceId, Principal principal) {
        User currentUser = getCurrentUser(principal); // à implémenter selon ton système d'auth
        Services service = serviceRepository.findById(serviceId).orElseThrow();
        User vendeur = service.getAuteur();
        return conversationService.getOrCreateConversation(currentUser, vendeur, serviceId);
    }


    private User getCurrentUser(Principal principal) {
        String username = principal.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
