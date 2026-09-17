package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Contrôleur global (ControllerAdvice) pour gérer l'état partagé entre tous les contrôleurs.
 * 
 * Responsabilités :
 * - Fournir des données globales aux vues (navbar, layout, etc.)
 * - Les données ajoutées via @ModelAttribute sont disponibles dans toutes les vues
 * 
 * Cas d'usage :
 * - Afficher le nombre d'articles dans le panier en temps réel dans la navbar
 * - Afficher l'utilisateur connecté
 * - Gérer les données partagées entre pages (erreurs globales, notifications, etc.)
 */
@ControllerAdvice
@RequiredArgsConstructor
public class StateController {

    /**
     * Repository pour accéder aux lignes de panier en base de données.
     * Injecté automatiquement par Spring.
     */
    private final CartLineRepository cartLineRepository;

    /**
     * Calcule et fournit le nombre d'articles dans le panier de l'utilisateur connecté.
     * Cette méthode est appelée automatiquement avant chaque rendu de vue.
     * Le résultat est disponible dans toutes les vues via la variable "cartCount".
     * 
     * Flux :
     * 1. Récupérer l'utilisateur connecté (null si anonyme)
     * 2. Si anonyme : retourner 0
     * 3. Si connecté : compter le nombre de CartLines de l'utilisateur
     * 
     * Utilisation dans la vue :
     * <a th:href="@{/cart}">Panier (<span th:text="${cartCount}">0</span>)</a>
     * 
     * @param user l'utilisateur actuellement connecté (null si anonyme)
     * @return le nombre total d'articles dans le panier (0 si pas d'articles ou utilisateur anonyme)
     */
    @ModelAttribute("cartCount")
    public int getCartCount(
            @AuthenticationPrincipal User user
    ) {
        // Si pas d'utilisateur connecté (utilisateur anonyme), retourner 0
        if(user == null) {
            return 0;
        }
        
        // Récupérer le nombre total de lignes de panier de l'utilisateur
        // Une CartLine = un produit dans le panier avec sa quantité
        return cartLineRepository.findByUserId(user.getId()).size();
    }
}
