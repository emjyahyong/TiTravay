package com.titravay.controller;

import com.titravay.model.User;
import com.titravay.repository.UserRepository;
import com.titravay.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class MessagesPageController {

    private final ConversationService conversationService;
    private final UserRepository      userRepository;

    public MessagesPageController(ConversationService conversationService,
                                  UserRepository userRepository) {
        this.conversationService = conversationService;
        this.userRepository      = userRepository;
    }

    @GetMapping("/messages")
    public String messagesPage(Model model, Principal principal) {
        return render(null, model, principal);
    }

    @GetMapping("/messages/{id}")
    public String messagesPageWithConv(@PathVariable Long id,
                                       Model model,
                                       Principal principal) {
        return render(id, model, principal);
    }

    private String render(Long activeConvId, Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        model.addAttribute("conversations", conversationService.getSummaries(principal));
        model.addAttribute("currentUser",   currentUser);
        model.addAttribute("activeConvId",  activeConvId);
        return "messages";
    }
}
