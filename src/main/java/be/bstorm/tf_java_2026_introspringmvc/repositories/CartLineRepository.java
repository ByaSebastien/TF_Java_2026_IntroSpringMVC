package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des lignes de panier en base de données.
 * Chaque CartLine représente un produit avec sa quantité dans un panier.
 * Les CartLines sont identifiées par une clé composite : (cart_id, product_id).
 */
@Repository
public interface CartLineRepository extends JpaRepository<CartLine, CartLine.CartLineId> {

    /**
     * Cherche toutes les lignes d'un panier spécifique.
     * Retourne tous les produits avec leurs quantités dans ce panier.
     * @param cartId l'ID du panier
     * @return une liste de CartLine appartenant à ce panier
     */
    @Query("select cl from CartLine cl join cl.cart c join cl.product where c.id = :cartId")
    List<CartLine> findByCartId (Long cartId);
}
