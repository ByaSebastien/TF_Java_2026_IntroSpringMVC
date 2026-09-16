package be.bstorm.tf_java_2026_introspringmvc.controllers;


import be.bstorm.tf_java_2026_introspringmvc.models.user.LoginForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour gérer l'authentification (login/logout).
 * Gère le formulaire de connexion pour les utilisateurs non authentifiés.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    /**
     * Affiche le formulaire de connexion.
     * Accessible uniquement aux utilisateurs anonymes (non connectés).
     * @PreAuthorize("isAnonymous()") empêche les utilisateurs déjà connectés d'accéder à cette page.
     * 
     * @param model l'objet pour passer le formulaire vide à la vue
     * @return le nom du template à afficher : "/auth/login"
     */
    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String login(
            Model model
    ) {

        model.addAttribute("form", new LoginForm());

        return "/auth/login";
    }
}
