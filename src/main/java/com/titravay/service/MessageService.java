package com.titravay.service;

import com.titravay.dto.MessageResponse;
import com.titravay.dto.SendMessageRequest;
import com.titravay.model.Conversation;
import com.titravay.model.Message;
import com.titravay.model.User;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.MessageRepository;
import com.titravay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository      messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository         userRepository;
    private final SimpMessagingTemplate  messagingTemplate;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository      = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository         = userRepository;
        this.messagingTemplate      = messagingTemplate;
    }

    // ── API publique ─────────────────────────────────────────

    /**
     * Persiste un message après avoir vérifié que l'expéditeur
     * est bien participant à la conversation.
     *
     * @throws ResponseStatusException 403 si l'utilisateur n'est pas participant
     */
    @Transactional
    public MessageResponse sendMessage(Long conversationId,
                                       SendMessageRequest request,
                                       Principal principal) {
        User sender = resolveUser(principal);
        Conversation conv = resolveParticipant(conversationId, sender.getId());

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setContent(request.getContent().strip());
        msg.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(msg);

        conv.setLastUpdated(saved.getTimestamp());
        conversationRepository.save(conv);

        MessageResponse response = toResponse(saved);

        // Notifier les deux participants sur leur canal privé (page Mes messages)
        String otherUsername = Objects.equals(conv.getParticipant1().getId(), sender.getId())
                ? conv.getParticipant2().getUsername()
                : conv.getParticipant1().getUsername();
        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(otherUsername,        "/queue/messages", response);

        log.debug("Message {} persisté dans conversation {}", saved.getId(), conversationId);
        return response;
    }

    /**
     * Retourne l'historique paginé d'une conversation.
     * Vérifie que l'appelant est participant avant tout accès.
     *
     * @throws ResponseStatusException 403 si l'utilisateur n'est pas participant
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long conversationId,
                                             Principal principal,
                                             Pageable pageable) {
        User user = resolveUser(principal);
        resolveParticipant(conversationId, user.getId());
        return messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId, pageable)
                .map(this::toResponse);
    }

    /**
     * Marque comme lus tous les messages non lus envoyés par l'autre participant.
     *
     * @return nombre de messages mis à jour
     * @throws ResponseStatusException 403 si l'utilisateur n'est pas participant
     */
    @Transactional
    public int markAsRead(Long conversationId, Principal principal) {
        User user = resolveUser(principal);
        resolveParticipant(conversationId, user.getId());
        int count = messageRepository.markMessagesAsRead(
                conversationId, user.getId(), LocalDateTime.now());
        if (count > 0) {
            log.debug("{} message(s) marqué(s) lu dans conversation {}", count, conversationId);
        }
        return count;
    }

    // ── Helpers privés ───────────────────────────────────────

    private User resolveUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Utilisateur inconnu : " + principal.getName()));
    }

    private Conversation resolveParticipant(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndParticipantId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Accès refusé à la conversation " + conversationId));
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation().getId(),
                m.getSender().getUsername(),
                m.getContent(),
                m.getTimestamp(),
                m.getReadAt()
        );
    }
}
