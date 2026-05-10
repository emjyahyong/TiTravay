package com.titravay.dto;

import java.time.LocalDateTime;

public record ConversationSummaryDTO(
        Long          id,
        String        otherUsername,
        String        otherInitial,
        Long          serviceId,
        String        serviceTitle,
        String        lastMessagePreview,
        LocalDateTime lastUpdated,
        long          unreadCount
) {}
