package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import be.bstorm.tf_java_2026_introspringmvc.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour gérer les opérations du panier utilisateur.
 * Permet aux utilisateurs authentifiés d'ajouter des produits à leur panier.
 * 
 * Routes disponibles : /cart/...
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    /**
     * Repository pour accéder aux utilisateurs en base de données.
     * Injecté automatiquement par Spring.
     */
    private final UserRepository userRepository;

    /**
     * Service métier pour gérer la logique du panier.
     * Injecté automatiquement par Spring.
     */
    private final CartService cartService;

    /**
     * Ajoute un produit au panier de l'utilisateur actuellement connecté.
     * Accessible uniquement aux utilisateurs authentifiés.
     * @AuthenticationPrincipal récupère automatiquement l'utilisateur connecté.
     * 
     * Flux :
     * 1. Récupérer le produit à ajouter par son ID
     * 2. Créer ou récupérer le panier de l'utilisateur
     * 3. Ajouter le produit au panier (ou augmenter la quantité si déjà présent)
     * 4. Rediriger vers la liste des produits
     * 
     * @param productId l'ID du produit à ajouter
     * @param user l'utilisateur actuellement connecté
     * @return redirection vers la page des produits (/product)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add/{productId}")
    public String addToCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ){

        // Ajouter le produit au panier via le service
        cartService.addToCart(user, productId);

        // Rediriger vers la liste des produits
        return "redirect:/product";
    }
}
