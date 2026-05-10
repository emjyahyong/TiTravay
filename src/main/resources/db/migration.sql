-- ============================================================
-- TiTravay — Migration manuelle
-- À exécuter une seule fois sur la base PostgreSQL cible.
-- ============================================================

-- 1. Statut de lecture des messages
ALTER TABLE message
    ADD COLUMN IF NOT EXISTS read_at TIMESTAMP NULL;

-- 2. Index composite pour la pagination des messages par conversation
--    (utilisé par MessageRepository.findByConversationIdOrderByTimestampAsc)
CREATE INDEX IF NOT EXISTS idx_message_conv_ts
    ON message (conversation_id, timestamp ASC);

-- 3. Index sur les participants pour les vérifications d'accès
--    (utilisé par ConversationRepository.findByIdAndParticipantId)
CREATE INDEX IF NOT EXISTS idx_conv_participant1
    ON conversation (participant1_id);

CREATE INDEX IF NOT EXISTS idx_conv_participant2
    ON conversation (participant2_id);
