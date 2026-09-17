package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.models.cart.CartDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import be.bstorm.tf_java_2026_introspringmvc.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
     * Affiche le panier de l'utilisateur actuellement connecté.
     * Accessible uniquement aux utilisateurs authentifiés.
     * @AuthenticationPrincipal récupère automatiquement l'utilisateur connecté.
     *
     * Flux :
     * 1. Récupérer le panier de l'utilisateur via le service
     * 2. Ajouter le panier au modèle pour l'affichage dans la vue
     * 3. Retourner le nom de la vue à afficher (cart.html)
     *
     * @param model le modèle pour passer des données à la vue
     * @param user l'utilisateur actuellement connecté
     * @return le nom de la vue à afficher (cart.html)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String getCart(
            Model model,
            @AuthenticationPrincipal User user
    ){
        // Récupérer le panier de l'utilisateur connecté via le service
        CartDto cart = CartDto.fromCartLines(cartService.getCartByUserId(user.getId()));

        // Ajouter le panier au modèle pour l'affichage dans la vue
        model.addAttribute("cart", cart);

        // Retourner le nom de la vue à afficher (cart.html)
        return "cart/details";
    }

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

    /**
     * Augmente la quantité d'un produit dans le panier de l'utilisateur.
     * Accessible uniquement aux utilisateurs authentifiés.
     * 
     * Flux :
     * 1. Récupérer l'utilisateur connecté
     * 2. Appeler le service pour augmenter la quantité du produit
     * 3. Rediriger vers la page du panier pour afficher les modifications
     * 
     * @param productId l'ID du produit dont augmenter la quantité
     * @param user l'utilisateur actuellement connecté
     * @return redirection vers le panier (/cart)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/increase/{productId}")
    public String increaseQuantity(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ){
        // Augmenter la quantité via le service
        cartService.increaseQuantity(user.getId(), productId);

        // Rediriger vers la page du panier
        return "redirect:/cart";
    }

    /**
     * Diminue la quantité d'un produit dans le panier de l'utilisateur.
     * Si la quantité tombe à 0, la ligne est automatiquement supprimée du panier.
     * Accessible uniquement aux utilisateurs authentifiés.
     * 
     * Flux :
     * 1. Récupérer l'utilisateur connecté
     * 2. Appeler le service pour diminuer la quantité du produit
     * 3. Rediriger vers la page du panier pour afficher les modifications
     * 
     * @param productId l'ID du produit dont diminuer la quantité
     * @param user l'utilisateur actuellement connecté
     * @return redirection vers le panier (/cart)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/decrease/{productId}")
    public String decreaseQuantity(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ){
        // Diminuer la quantité via le service
        cartService.decreaseQuantity(user.getId(), productId);

        // Rediriger vers la page du panier
        return "redirect:/cart";
    }

    /**
     * Supprime complètement un produit du panier de l'utilisateur.
     * Peu importe la quantité, la ligne entière est supprimée.
     * Accessible uniquement aux utilisateurs authentifiés.
     * 
     * Flux :
     * 1. Récupérer l'utilisateur connecté
     * 2. Appeler le service pour supprimer la ligne du panier
     * 3. Rediriger vers la page du panier pour afficher les modifications
     * 
     * @param productId l'ID du produit à supprimer du panier
     * @param user l'utilisateur actuellement connecté
     * @return redirection vers le panier (/cart)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove/{productId}")
    public String removeFromCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ){
        // Supprimer la ligne du panier via le service
        cartService.removeFromCart(user.getId(), productId);

        // Rediriger vers la page du panier
        return "redirect:/cart";
    }
}
