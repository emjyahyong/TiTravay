package com.titravay.service;

import com.titravay.model.*;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.ServiceRepository;
import com.titravay.model.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ServiceRepository serviceRepository; // Pour récupérer l'objet Service

    public Conversation getOrCreateConversation(User user1, User user2, Long serviceId) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service non trouvé"));

        return conversationRepository
                .findByParticipant1AndParticipant2AndService(user1, user2, service)
                .or(() -> conversationRepository.findByParticipant2AndParticipant1AndService(user1, user2, service))
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setParticipant1(user1);
                    c.setParticipant2(user2);
                    c.setService(service);
                    c.setLastUpdated(LocalDateTime.now());
                    return conversationRepository.save(c);
                });
    }
}
