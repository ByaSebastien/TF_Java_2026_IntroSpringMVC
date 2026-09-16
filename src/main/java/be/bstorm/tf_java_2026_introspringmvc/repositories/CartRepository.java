package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'accès aux données des paniers en base de données.
 * Fournit les opérations CRUD standard ainsi que des requêtes personnalisées pour trouver un panier.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Cherche le panier d'un utilisateur spécifique.
     * Chaque utilisateur ne doit avoir qu'un seul panier actif.
     * @param userId l'ID de l'utilisateur
     * @return un Optional contenant le panier s'il existe, vide sinon
     */
    @Query("select c from Cart c join c.user u where u.id = :userId")
    Optional<Cart> findByUserId(Long userId);
}
