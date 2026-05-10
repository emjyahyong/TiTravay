package com.titravay.controller;

import com.titravay.model.User;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class ConversationPageController {

    private final UserRepository         userRepository;
    private final ConversationRepository conversationRepository;

    public ConversationPageController(UserRepository userRepository,
                                      ConversationRepository conversationRepository) {
        this.userRepository         = userRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * Affiche la page de conversation.
     * Retourne 403 si l'utilisateur n'est pas participant à la conversation,
     * sans distinguer le cas "introuvable" pour éviter l'énumération d'IDs.
     */
    @GetMapping("/conversations/{id}")
    public String showConversation(@PathVariable Long id,
                                   Model model,
                                   Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        conversationRepository.findByIdAndParticipantId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Accès refusé à cette conversation"));

        model.addAttribute("conversationId", id);
        model.addAttribute("currentUser", currentUser);
        return "conversation";
    }
}
