package com.titravay.controller;

import com.titravay.model.Service;
import com.titravay.model.User;
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

@Controller
@RequestMapping("services")
public class ServiceController {

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
        service.setDatePublication(LocalDateTime.now());
        service.setStatut(Service.StatutService.ACTIVE);
        // Si tu veux associer l'utilisateur connecté :
        // service.setAuteur(authenticatedUser);
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
