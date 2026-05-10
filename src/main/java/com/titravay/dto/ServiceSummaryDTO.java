package com.titravay.dto;

import com.titravay.model.Services;

import java.time.LocalDateTime;

public record ServiceSummaryDTO(
        Long                     id,
        String                   titre,
        Services.Categorie       categorie,
        Double                   prix,
        Services.StatutService   statut,
        LocalDateTime            datePublication
) {}
