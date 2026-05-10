package com.titravay.repository;

import com.titravay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    /** Vérifie l'unicité du username en excluant l'utilisateur lui-même (modification de profil). */
    boolean existsByUsernameAndIdNot(String username, Long id);
}