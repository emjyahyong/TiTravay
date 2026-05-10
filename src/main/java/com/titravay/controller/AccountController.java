package com.titravay.controller;

import com.titravay.dto.ChangePasswordRequest;
import com.titravay.dto.UpdateProfileRequest;
import com.titravay.model.User;
import com.titravay.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/compte")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // ── Navigation ────────────────────────────────────────────

    @GetMapping
    public String root() {
        return "redirect:/compte/profil";
    }

    // ── Section Profil ────────────────────────────────────────

    @GetMapping("/profil")
    public String profilPage(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.resolveUser(ud.getUsername());
        model.addAttribute("profile",       userService.getProfile(user));
        model.addAttribute("activeSection", "profil");
        return "compte/profil";
    }

    @PostMapping("/profil")
    public String updateProfil(@AuthenticationPrincipal UserDetails ud,
                               @Valid UpdateProfileRequest request,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/compte/profil";
        }
        try {
            User user = userService.resolveUser(ud.getUsername());
            boolean usernameChanged = userService.updateProfile(user, request);
            if (usernameChanged) {
                // Le username en session est désormais périmé — reconnexion requise
                SecurityContextHolder.clearContext();
                session.invalidate();
                return "redirect:/login?username-change";
            }
            ra.addFlashAttribute("successMessage", "Profil mis à jour avec succès.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/compte/profil";
    }

    // ── Section Sécurité ──────────────────────────────────────

    @GetMapping("/securite")
    public String securitePage(Model model) {
        model.addAttribute("activeSection", "securite");
        return "compte/securite";
    }

    @PostMapping("/mot-de-passe")
    public String changePassword(@AuthenticationPrincipal UserDetails ud,
                                 @Valid ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/compte/securite";
        }
        try {
            User user = userService.resolveUser(ud.getUsername());
            userService.changePassword(user, request);
            ra.addFlashAttribute("successMessage",
                    "Mot de passe modifié avec succès.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/compte/securite";
    }

    // ── Section Mes services ──────────────────────────────────

    @GetMapping("/services")
    public String servicesPage(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.resolveUser(ud.getUsername());
        model.addAttribute("services",      userService.getUserServices(user));
        model.addAttribute("activeSection", "services");
        return "compte/services";
    }

    @PostMapping("/services/{id}/supprimer")
    public String deleteService(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes ra) {
        try {
            User user = userService.resolveUser(ud.getUsername());
            userService.deleteService(id, user);
            ra.addFlashAttribute("successMessage", "Service supprimé.");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("errorMessage", e.getReason());
        }
        return "redirect:/compte/services";
    }

    // ── Suppression de compte ─────────────────────────────────

    @PostMapping("/supprimer")
    public String deleteAccount(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String confirmUsername,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (!ud.getUsername().equals(confirmUsername)) {
            ra.addFlashAttribute("errorMessage",
                    "Le nom d'utilisateur saisi ne correspond pas. Suppression annulée.");
            return "redirect:/compte/profil";
        }
        try {
            User user = userService.resolveUser(ud.getUsername());
            userService.deleteAccount(user);
        } finally {
            // Déconnexion garantie même en cas d'erreur partielle
            SecurityContextHolder.clearContext();
            session.invalidate();
        }
        return "redirect:/?compte-supprime";
    }
}
