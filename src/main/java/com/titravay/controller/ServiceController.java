package com.titravay.controller;

import com.titravay.model.Service;
import com.titravay.model.User;
import com.titravay.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

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

}
