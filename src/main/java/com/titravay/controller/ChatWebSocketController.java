package com.titravay.controller;

import com.titravay.model.Conversation;
import com.titravay.model.Message;
import com.titravay.repository.ConversationRepository;
import com.titravay.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}")
    public Message sendMessage(@DestinationVariable Long conversationId, @Payload Message message) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        message.setConversation(conversation);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

}

