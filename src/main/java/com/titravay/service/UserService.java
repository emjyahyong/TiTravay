package com.titravay.service;

import com.titravay.dto.ChangePasswordRequest;
import com.titravay.dto.ServiceSummaryDTO;
import com.titravay.dto.UpdateProfileRequest;
import com.titravay.dto.UserProfileDTO;
import com.titravay.model.Conversation;
import com.titravay.model.Services;
import com.titravay.model.User;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.MessageRepository;
import com.titravay.repository.ServiceRepository;
import com.titravay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository         userRepository;
    private final ServiceRepository      serviceRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository      messageRepository;
    private final PasswordEncoder        passwordEncoder;

    public UserService(UserRepository userRepository,
                       ServiceRepository serviceRepository,
                       ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository         = userRepository;
        this.serviceRepository      = serviceRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository      = messageRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    // ── Résolution ────────────────────────────────────────────

    /** Retrouve le domaine User à partir du username de la session Spring Security. */
    public User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Utilisateur inconnu : " + username));
    }

    // ── Profil ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(User user) {
        return new UserProfileDTO(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * Met à jour le username de l'utilisateur.
     * Lève {@link IllegalArgumentException} si le username est déjà pris.
     *
     * @return true si le username a changé (nécessite re-login côté contrôleur)
     */
    @Transactional
    public boolean updateProfile(User user, UpdateProfileRequest request) {
        String newUsername = request.getUsername().strip();
        boolean changed = !user.getUsername().equals(newUsername);

        if (changed) {
            if (userRepository.existsByUsernameAndIdNot(newUsername, user.getId())) {
                throw new IllegalArgumentException(
                        "Le nom d'utilisateur « " + newUsername + " » est déjà pris");
            }
            log.info("Changement de username : {} → {}", user.getUsername(), newUsername);
            user.setUsername(newUsername);
            userRepository.save(user);
        }

        return changed;
    }

    // ── Mot de passe ──────────────────────────────────────────

    /**
     * Change le mot de passe après vérification de l'ancien et de la correspondance.
     * Lève {@link IllegalArgumentException} si une règle n'est pas respectée.
     */
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Mot de passe modifié pour l'utilisateur {}", user.getUsername());
        // Pas de log du mot de passe même hashé
    }

    // ── Services ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ServiceSummaryDTO> getUserServices(User user) {
        return serviceRepository.findByAuteurOrderByDatePublicationDesc(user)
                .stream()
                .map(s -> new ServiceSummaryDTO(
                        s.getId(), s.getTitre(), s.getCategorie(),
                        s.getPrix(), s.getStatut(), s.getDatePublication()))
                .toList();
    }

    /**
     * Supprime un service appartenant à l'utilisateur, avec ses conversations et messages.
     * Lève 403 si le service n'appartient pas à l'utilisateur.
     */
    @Transactional
    public void deleteService(Long serviceId, User user) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service introuvable"));

        if (!service.getAuteur().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas propriétaire de ce service");
        }

        deleteConversationsForService(serviceId);
        serviceRepository.delete(service);
        log.info("Service {} supprimé par {}", serviceId, user.getUsername());
    }

    // ── Suppression de compte ─────────────────────────────────

    /**
     * Supprime le compte en cascade : messages → conversations → services → user.
     * Ordre garanti pour ne pas violer les contraintes FK.
     */
    @Transactional
    public void deleteAccount(User user) {
        log.warn("Suppression du compte : {}", user.getUsername());

        // 1. Toutes les conversations du user
        List<Conversation> convs = conversationRepository
                .findAllByParticipantIdOrderByLastUpdatedDesc(user.getId());

        // 2. Messages dans ces conversations
        if (!convs.isEmpty()) {
            List<Long> ids = convs.stream().map(Conversation::getId).toList();
            messageRepository.deleteAllByConversationIdIn(ids);
        }

        // 3. Conversations
        conversationRepository.deleteAll(convs);

        // 4. Services de l'utilisateur (leurs conversations ont déjà été supprimées ci-dessus)
        List<Services> services = serviceRepository.findByAuteur(user);
        serviceRepository.deleteAll(services);

        // 5. User
        userRepository.delete(user);
    }

    // ── Helpers privés ────────────────────────────────────────

    private void deleteConversationsForService(Long serviceId) {
        List<Conversation> convs = conversationRepository.findAllByServiceId(serviceId);
        if (!convs.isEmpty()) {
            List<Long> ids = convs.stream().map(Conversation::getId).toList();
            messageRepository.deleteAllByConversationIdIn(ids);
            conversationRepository.deleteAll(convs);
        }
    }
}
