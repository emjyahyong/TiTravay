package com.titravay.repository;

import com.titravay.model.Conversation;
import com.titravay.model.Services;
import com.titravay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  Optional<Conversation> findByParticipant1AndParticipant2AndService(User p1, User p2, Services service);
  Optional<Conversation> findByParticipant2AndParticipant1AndService(User p1, User p2, Services service);
}
