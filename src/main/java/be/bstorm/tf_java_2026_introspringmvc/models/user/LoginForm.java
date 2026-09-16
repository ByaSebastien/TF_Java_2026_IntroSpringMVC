package be.bstorm.tf_java_2026_introspringmvc.models.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Formulaire de connexion pour l'authentification.
 * Contient les identifiants d'un utilisateur pour se connecter.
 * Les annotations @NotBlank garantissent que l'utilisateur remplit les champs.
 */
public class LoginForm {

    /**
     * Nom d'utilisateur pour la connexion.
     * Ne doit pas être vide.
     */
    @NotBlank
    private String username;

    /**
     * Mot de passe pour la connexion.
     * Ne doit pas être vide.
     * Attention : jamais stocker le mot de passe en clair (voir SecurityConfig.passwordEncoder()).
     */
    @NotBlank
    private String password;

    // Getters et setters générés par Lombok @Data
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
