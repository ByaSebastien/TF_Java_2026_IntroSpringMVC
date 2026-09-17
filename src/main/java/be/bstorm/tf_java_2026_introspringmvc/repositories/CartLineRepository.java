package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des lignes de panier en base de données.
 * Chaque CartLine représente un produit avec sa quantité dans un panier.
 * Les CartLines sont identifiées par une clé composite : (cart_id, product_id).
 * 
 * Responsabilités :
 * - Requêtes de lecture/écriture des CartLines en base de données
 * - Recherches spécifiques (par panier, par utilisateur)
 */
@Repository
public interface CartLineRepository extends JpaRepository<CartLine, CartLine.CartLineId> {

    /**
     * Cherche toutes les lignes d'un panier spécifique.
     * Retourne tous les produits avec leurs quantités dans ce panier.
     * 
     * @param cartId l'ID du panier
     * @return une liste de CartLine appartenant à ce panier
     */
    @Query("select cl from CartLine cl join cl.cart c join cl.product where c.id = :cartId")
    List<CartLine> findByCartId(Long cartId);

    /**
     * Cherche toutes les lignes du panier d'un utilisateur spécifique.
     * Traverse la relation panier -> utilisateur pour récupérer les produits du panier.
     * 
     * @param userId l'ID de l'utilisateur propriétaire du panier
     * @return une liste de CartLine correspondant à tous les produits dans le panier de cet utilisateur
     */
    @Query("select cl from CartLine cl join cl.cart c join cl.product where c.user.id = :userId")
    List<CartLine> findByUserId(Long userId);

    /**
     * Cherche une ligne de panier spécifique pour un utilisateur et un produit donnés.
     * Utilisé pour charger la CartLine avant de la modifier et la sauvegarder.
     * 
     * @param userId l'ID de l'utilisateur
     * @param productId l'ID du produit
     * @return un Optional contenant la CartLine si elle existe, vide sinon
     */
    @Query("select cl from CartLine cl " +
           "where cl.cart.user.id = :userId and cl.product.id = :productId")
    Optional<CartLine> findByUserAndProduct(Long userId, Long productId);
}
