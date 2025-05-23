package com.titravay.controller;

import com.titravay.model.Conversation;
import com.titravay.model.User;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class ConversationPageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping("/conversations/{id}")
    public String showConversation(@PathVariable Long id, Model model, Principal principal) {
        Conversation c = conversationRepository.findById(id).orElseThrow();

        // Récupère le nom d'utilisateur à partir du principal
        String username = principal.getName();

        // Récupère l'utilisateur depuis la base
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        model.addAttribute("conversationId", id);
        model.addAttribute("currentUser", currentUser);

        return "conversation"; // conversation.html
    }

}
