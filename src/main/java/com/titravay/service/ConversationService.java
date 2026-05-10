package com.titravay.service;

import com.titravay.dto.ConversationStartResponse;
import com.titravay.model.Conversation;
import com.titravay.model.Services;
import com.titravay.model.User;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.ServiceRepository;
import com.titravay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final ServiceRepository      serviceRepository;
    private final UserRepository         userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ServiceRepository serviceRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.serviceRepository      = serviceRepository;
        this.userRepository         = userRepository;
    }

    /**
     * Crée ou retrouve la conversation entre l'utilisateur courant
     * et le prestataire du service identifié par {@code serviceId}.
     * Retourne un DTO minimal (id) pour ne pas exposer l'entité brute.
     *
     * @throws ResponseStatusException 404 service inexistant
     * @throws ResponseStatusException 400 si l'utilisateur tente de se contacter lui-même
     */
    @Transactional
    public ConversationStartResponse getOrCreate(Long serviceId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Utilisateur inconnu"));

        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service introuvable : " + serviceId));

        User vendor = service.getAuteur();

        if (Objects.equals(currentUser.getId(), vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Impossible de démarrer une conversation avec vous-même");
        }

        Conversation conv = conversationRepository
                .findByParticipant1AndParticipant2AndService(currentUser, vendor, service)
                .or(() -> conversationRepository
                        .findByParticipant2AndParticipant1AndService(currentUser, vendor, service))
                .orElseGet(() -> {
                    log.info("Nouvelle conversation : service={} entre {} et {}",
                            serviceId, currentUser.getUsername(), vendor.getUsername());
                    Conversation c = new Conversation();
                    c.setParticipant1(currentUser);
                    c.setParticipant2(vendor);
                    c.setService(service);
                    c.setLastUpdated(LocalDateTime.now());
                    return conversationRepository.save(c);
                });

        return new ConversationStartResponse(conv.getId());
    }
}
