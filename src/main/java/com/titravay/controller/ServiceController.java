package com.titravay.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.titravay.model.User;
import com.titravay.repository.UserRepository;
import com.titravay.model.Service;
import com.titravay.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("services")
public class ServiceController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("categories", Service.Categorie.values());
        return "service/form";
    }

    @PostMapping("/add")
    public String submitForm(@ModelAttribute Service service) {
        // Récupérer l'utilisateur connecté (nom d'utilisateur)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Charger l'objet User complet depuis la base
        Optional<User> utilisateurConnecte = userRepository.findByUsername(username);

        // Vérifier que l'utilisateur existe
        if (utilisateurConnecte.isPresent()) {
            service.setAuteur(utilisateurConnecte.get());
        } else {
            // Gérer le cas où l'utilisateur n'est pas trouvé (sécurité / erreur)
            throw new RuntimeException("Utilisateur connecté non trouvé dans la base");
        }

        service.setDatePublication(LocalDateTime.now());
        service.setStatut(Service.StatutService.ACTIVE);
        serviceRepository.save(service);

        return "redirect:/home";
    }

    @GetMapping("/{id}")
    public String showServiceDetail(@PathVariable Long id, Model model) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        String dateFormatee = service.getDatePublication().format(formatter);
        model.addAttribute("dateFormatee", dateFormatee);
        model.addAttribute("service", service);
        return "service/detail"; // ← Ce nom doit correspondre au chemin src/main/resources/templates/service/detail.html
    }
}
