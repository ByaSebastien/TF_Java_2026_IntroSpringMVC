package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des commandes client en base de données.
 * 
 * Responsabilités :
 * - Opérations CRUD sur les commandes
 * - Requêtes de filtrage par statut
 * - Recherche de commandes par utilisateur
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Cherche toutes les commandes avec un statut spécifique.
     * 
     * Flux SQL :
     * SELECT o FROM Order o WHERE o.status = :status
     * 
     * @param status le statut des commandes à récupérer
     * @return une liste de commandes avec ce statut
     */
    @Query("select o from Order o where o.status = :status")
    List<Order> findByStatus(OrderStatus status);

    /**
     * Cherche toutes les commandes d'un utilisateur spécifique.
     * 
     * @param userId l'ID de l'utilisateur
     * @return une liste de commandes de cet utilisateur
     */
    @Query("select o from Order o where o.user.id = :userId")
    List<Order> findByUserId(Long userId);

    /**
     * Cherche toutes les commandes en statut PENDING (en attente de validation).
     * Utilisé par le magasinier pour voir ses tâches.
     * 
     * @return une liste de commandes en attente
     */
    @Query("select o from Order o where o.status = 'PENDING' order by o.createdAt desc")
    List<Order> findPendingOrders();

    /**
     * Cherche toutes les commandes d'un utilisateur avec un statut spécifique.
     * Utilisé pour l'historique client.
     * 
     * @param userId l'ID de l'utilisateur
     * @param status le statut recherché
     * @return une liste de commandes filtrées
     */
    @Query("select o from Order o where o.user.id = :userId and o.status = :status")
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}
