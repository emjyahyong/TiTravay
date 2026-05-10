package com.titravay.repository;

import com.titravay.model.Conversation;
import com.titravay.model.Services;
import com.titravay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParticipant1AndParticipant2AndService(User p1, User p2, Services service);
    Optional<Conversation> findByParticipant2AndParticipant1AndService(User p2, User p1, Services service);

    /**
     * Retrouve une conversation par son id uniquement si {@code userId} en est participant.
     * Utilisé pour les vérifications d'accès dans les services et controllers.
     * Retourne vide (→ 403) si la conversation n'existe pas OU si l'utilisateur n'est pas participant.
     */
    @Query(
        "SELECT c FROM Conversation c " +
        "WHERE c.id = :convId " +
        "  AND (c.participant1.id = :userId OR c.participant2.id = :userId)"
    )
    Optional<Conversation> findByIdAndParticipantId(
            @Param("convId") Long convId,
            @Param("userId") Long userId
    );
}
