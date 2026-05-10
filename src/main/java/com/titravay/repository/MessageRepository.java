package com.titravay.repository;

import com.titravay.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Historique paginé d'une conversation, trié chronologiquement.
     * JOIN FETCH sur sender pour éviter les requêtes N+1.
     * countQuery séparée pour que Hibernate ne joigne pas inutilement lors du COUNT.
     */
    @Query(
        value      = "SELECT m FROM Message m JOIN FETCH m.sender " +
                     "WHERE m.conversation.id = :conversationId " +
                     "ORDER BY m.timestamp ASC",
        countQuery = "SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId"
    )
    Page<Message> findByConversationIdOrderByTimestampAsc(
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );

    /**
     * Marque comme lus tous les messages non lus d'une conversation
     * qui ont été envoyés par quelqu'un d'autre que {@code userId}.
     *
     * @return nombre de lignes mises à jour
     */
    @Modifying
    @Query(
        "UPDATE Message m SET m.readAt = :now " +
        "WHERE m.conversation.id = :conversationId " +
        "  AND m.sender.id <> :userId " +
        "  AND m.readAt IS NULL"
    )
    int markMessagesAsRead(
            @Param("conversationId") Long conversationId,
            @Param("userId")         Long userId,
            @Param("now")            LocalDateTime now
    );

    /** Dernier message d'une conversation (pour l'aperçu dans la sidebar). */
    Optional<Message> findTopByConversationIdOrderByTimestampDesc(Long conversationId);

    /** Nombre de messages non lus envoyés par l'autre participant. */
    @Query(
        "SELECT COUNT(m) FROM Message m " +
        "WHERE m.conversation.id = :conversationId " +
        "  AND m.sender.id <> :userId " +
        "  AND m.readAt IS NULL"
    )
    long countUnreadByConversationIdAndUserId(
            @Param("conversationId") Long conversationId,
            @Param("userId")         Long userId
    );
}
