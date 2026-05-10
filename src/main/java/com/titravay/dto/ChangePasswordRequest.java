package com.titravay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequest {

    @NotBlank(message = "Le mot de passe actuel est requis")
    private String currentPassword;

    @NotBlank(message = "Le nouveau mot de passe est requis")
    @Pattern(
        regexp  = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre"
    )
    private String newPassword;

    @NotBlank(message = "Veuillez confirmer le nouveau mot de passe")
    private String confirmPassword;

    public String getCurrentPassword()              { return currentPassword; }
    public void   setCurrentPassword(String p)      { this.currentPassword = p; }

    public String getNewPassword()                  { return newPassword; }
    public void   setNewPassword(String p)          { this.newPassword = p; }

    public String getConfirmPassword()              { return confirmPassword; }
    public void   setConfirmPassword(String p)      { this.confirmPassword = p; }
}
