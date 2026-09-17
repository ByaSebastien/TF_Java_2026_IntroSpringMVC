package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des lignes de commande client en base de données.
 * 
 * Responsabilités :
 * - Opérations CRUD sur les lignes
 * - Requêtes pour récupérer les lignes d'une commande
 */
@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, OrderLine.OrderLineId> {

    /**
     * Cherche toutes les lignes d'une commande spécifique.
     * Utilisé pour afficher les détails d'une commande.
     * 
     * Flux SQL :
     * SELECT ol FROM OrderLine ol WHERE ol.order.id = :orderId
     * 
     * @param orderId l'ID de la commande
     * @return une liste de lignes appartenant à cette commande
     */
    @Query("select ol from OrderLine ol where ol.order.id = :orderId")
    List<OrderLine> findByOrderId(Long orderId);

    /**
     * Cherche toutes les lignes d'une commande, jointurées avec produit et commande.
     * Optimisée pour éviter les N+1 queries.
     * 
     * @param orderId l'ID de la commande
     * @return une liste de lignes avec données complètes
     */
    @Query("select ol from OrderLine ol " +
           "join fetch ol.product " +
           "join fetch ol.order " +
           "where ol.order.id = :orderId")
    List<OrderLine> findByOrderIdWithFetch(Long orderId);

    /**
     * Supprime toutes les lignes d'une commande.
     * Utilisé lors de la suppression d'une commande.
     * 
     * @param orderId l'ID de la commande
     * @return le nombre de lignes supprimées
     */
    @Query("delete from OrderLine ol where ol.order.id = :orderId")
    int deleteByOrderId(Long orderId);
}
