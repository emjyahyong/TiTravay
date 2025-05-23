package com.titravay.controller;

import com.titravay.model.Services;
import com.titravay.model.User;
import com.titravay.repository.ServiceRepository;
import com.titravay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<Services> Services = (List<Services>) serviceRepository.findAll();
        model.addAttribute("services", Services);
        return "home"; // Fichier home.html dans /templates
    }
    
    @PostMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               Model model) {
        if (userRepo.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà pris.");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // ✅
        newUser.setRole(role);

        userRepo.save(newUser);
        return "redirect:/login";
    }
}
