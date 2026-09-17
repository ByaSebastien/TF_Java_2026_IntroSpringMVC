package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Service métier pour gérer les opérations du panier.
 * Contient la logique pour ajouter des produits au panier d'un utilisateur.
 * <p>
 * Flux d'ajout au panier :
 * 1. Vérifier que le produit existe
 * 2. Chercher ou créer le panier de l'utilisateur
 * 3. Vérifier si le produit est déjà dans le panier
 * - Si oui : augmenter la quantité
 * - Si non : créer une nouvelle ligne de panier
 */
@Service
@RequiredArgsConstructor
public class CartService {

    /**
     * Repository pour accéder aux paniers en base de données.
     * Injecté automatiquement par Spring.
     */
    private final CartRepository cartRepository;

    /**
     * Repository pour accéder aux produits en base de données.
     * Injecté automatiquement par Spring.
     */
    private final ProductRepository productRepository;

    /**
     * Repository pour accéder aux lignes de panier en base de données.
     * Injecté automatiquement par Spring.
     */
    private final CartLineRepository cartLineRepository;

    /**
     * Ajoute un produit au panier d'un utilisateur.
     * Si le produit est déjà dans le panier, augmente simplement la quantité.
     * Si le produit n'est pas encore dans le panier, crée une nouvelle ligne.
     *
     * @param user      l'utilisateur dont on veut modifier le panier
     * @param productId l'ID du produit à ajouter
     * @throws NoSuchElementException si le produit n'existe pas
     */
    public void addToCart(User user, Long productId) {
        // 1. Récupérer le produit (exception si inexistant)
        Product product = productRepository.findById(productId).orElseThrow();

        // 2. Créer le panier si inexistant, sinon le récupérer
        Cart cart = computeIfAbsent(user);

        // 3. Récupérer toutes les lignes du panier
        List<CartLine> cartLines = cartLineRepository.findByCartId(cart.getId());

        // 4. Chercher si le produit est déjà dans le panier
        Optional<CartLine> existingLine = cartLines.stream()
                .filter(cl -> cl.getProduct().getId().equals(productId))
                .findFirst();

        if (existingLine.isPresent()) {
            // Le produit existe déjà : augmenter la quantité
            CartLine line = existingLine.get();
            line.setQuantity(line.getQuantity() + 1);
            cartLineRepository.save(line);
        } else {
            // Le produit n'est pas dans le panier : créer une nouvelle ligne
            CartLine line = new CartLine(1, cart, product);
            cartLineRepository.save(line);
        }
    }

    /**
     * Cherche ou crée le panier d'un utilisateur.
     * Si l'utilisateur n'a pas encore de panier, en crée un nouveau.
     *
     * @param user l'utilisateur
     * @return le panier de l'utilisateur (créé s'il n'existait pas)
     */
    private Cart computeIfAbsent(User user) {
        // Chercher le panier existant
        Optional<Cart> cart = cartRepository.findByUserId(user.getId());

        Cart existingCart;

        if (cart.isEmpty()) {
            // Panier inexistant : créer un nouveau
            Cart newCart = new Cart(user);
            existingCart = cartRepository.save(newCart);
        } else {
            // Panier existe : le récupérer
            existingCart = cart.get();
        }

        return existingCart;
    }

    /**
     * Récupère le panier complet d'un utilisateur par son ID.
     * Retourne toutes les lignes du panier de cet utilisateur.
     *
     * @param userId l'ID de l'utilisateur
     * @return une liste de CartLine représentant tous les produits dans le panier de l'utilisateur
     */
    public List<CartLine> getCartByUserId(Long userId) {
        return cartLineRepository.findByUserId(userId);
    }

    /**
     * Augmente la quantité d'un produit dans le panier d'un utilisateur.
     * <p>
     * Flux :
     * 1. Charger la CartLine de l'utilisateur et du produit
     * 2. Si elle existe : incrémenter la quantité
     * 3. Sauvegarder la CartLine
     *
     * @param userId    l'ID de l'utilisateur
     * @param productId l'ID du produit à augmenter
     */
    public void increaseQuantity(Long userId, Long productId) {
        // Charger la CartLine
        CartLine line = cartLineRepository.findByUserAndProduct(userId, productId)
                .orElseThrow();

        line.setQuantity(line.getQuantity() + 1);

        cartLineRepository.save(line);
    }

    /**
     * Diminue la quantité d'un produit dans le panier d'un utilisateur.
     * Si la quantité tombe à 0, la ligne est automatiquement supprimée du panier.
     * <p>
     * Flux :
     * 1. Charger la CartLine de l'utilisateur et du produit
     * 2. Si elle existe :
     * - Décrémenter la quantité
     * - Si quantité = 0 : supprimer la ligne
     * - Sinon : sauvegarder la CartLine modifiée
     *
     * @param userId    l'ID de l'utilisateur
     * @param productId l'ID du produit à diminuer
     */
    public void decreaseQuantity(Long userId, Long productId) {
        // Charger la CartLine
        CartLine line = cartLineRepository.findByUserAndProduct(userId, productId)
                .orElseThrow();

        line.setQuantity(line.getQuantity() - 1);

        if (line.getQuantity() == 0) {
            cartLineRepository.delete(line);
        } else {
            cartLineRepository.save(line);
        }
    }

    /**
     * Supprime complètement un produit du panier d'un utilisateur.
     * La ligne entière est supprimée, peu importe la quantité.
     * <p>
     * Délègue directement au repository qui effectue une requête DELETE en base.
     *
     * @param userId    l'ID de l'utilisateur
     * @param productId l'ID du produit à supprimer
     */
    public void removeFromCart(Long userId, Long productId) {
        CartLine cartLine = cartLineRepository.findByUserAndProduct(userId, productId)
                .orElseThrow();

        cartLineRepository.delete(cartLine);
    }
}
