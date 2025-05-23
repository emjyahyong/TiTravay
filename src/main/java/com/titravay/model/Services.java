package com.titravay.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class Services implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String description;

    private Double prix;

    private LocalDateTime datePublication;

    private String localisation;

    private String imageUrl; // ou List<String> si tu veux plusieurs images

    @Enumerated(EnumType.STRING)
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User auteur;

    // Statut de l'Service (active, vendue, archivée...)
    @Enumerated(EnumType.STRING)
    private StatutService statut;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public enum Categorie {
        BRICOLAGE, JARDINAGE, MENAGE, INFORMATIQUE, COURS_PARTICULIERS, BEAUTE_BIENETRE, TRANSPORT, ASSISTANCE_ADMINISTRATIVE, EVENEMENTIEL, AUTRES
        }

    public enum StatutService {
        ACTIVE, INACTIVE
    }

    public Services(Long id, String titre, String description, Double prix, LocalDateTime datePublication, String localisation, String imageUrl, Categorie categorie, User auteur, StatutService statut) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.datePublication = datePublication;
        this.localisation = localisation;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
        this.auteur = auteur;
        this.statut = statut;
    }

    public Services() {
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public User getAuteur() {
        return auteur;
    }

    public void setAuteur(User auteur) {
        this.auteur = auteur;
    }

    public StatutService getStatut() {
        return statut;
    }

    public void setStatut(StatutService statut) {
        this.statut = statut;
    }
}
