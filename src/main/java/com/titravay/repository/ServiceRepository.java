package com.titravay.repository;

import com.titravay.model.Services;
import com.titravay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {
    List<Services> findByCategorie(Services.Categorie categorie);
    List<Services> findByAuteur(User user);

    /** Services d'un utilisateur, triés par date de publication décroissante. */
    List<Services> findByAuteurOrderByDatePublicationDesc(User auteur);
}