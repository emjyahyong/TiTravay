package com.titravay.repository;

import com.titravay.model.Service;
import com.titravay.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends CrudRepository<Service, Long> {
    List<Service> findByCategorie(Service.Categorie categorie);
    List<Service> findByAuteur(User user);
}