package com.titravay.repository;

import com.titravay.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
