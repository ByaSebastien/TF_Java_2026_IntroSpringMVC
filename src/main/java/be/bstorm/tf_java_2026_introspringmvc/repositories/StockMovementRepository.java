package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.StockMovement;
import be.bstorm.tf_java_2026_introspringmvc.enums.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des mouvements de stock en base de données.
 * 
 * Responsabilités :
 * - Enregistrement des mouvements (entrées/sorties)
 * - Requêtes d'historique par produit, commande client ou fournisseur
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * Cherche tous les mouvements de stock d'un produit spécifique.
     * Utilisé pour afficher l'historique des stocks d'un produit.
     * 
     * @param productId l'ID du produit
     * @return une liste de mouvements pour ce produit (triés par date desc)
     */
    @Query("select sm from StockMovement sm " +
           "where sm.product.id = :productId " +
           "order by sm.movementDate desc")
    List<StockMovement> findByProductId(Long productId);

    /**
     * Cherche tous les mouvements de stock liés à une commande client.
     * Utilisé pour tracer les sorties de stock d'une commande.
     * 
     * @param orderId l'ID de la commande client
     * @return une liste de mouvements OUTGOING pour cette commande
     */
    @Query("select sm from StockMovement sm " +
           "where sm.order.id = :orderId " +
           "order by sm.movementDate desc")
    List<StockMovement> findByOrderId(Long orderId);

    /**
     * Cherche tous les mouvements de stock liés à une commande fournisseur.
     * Utilisé pour tracer les entrées de stock d'une réception.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @return une liste de mouvements INCOMING pour cette commande
     */
    @Query("select sm from StockMovement sm " +
           "where sm.supplierOrder.id = :supplierOrderId " +
           "order by sm.movementDate desc")
    List<StockMovement> findBySupplierOrderId(Long supplierOrderId);

    /**
     * Cherche tous les mouvements de stock d'un type spécifique.
     * Utilisé pour les statistiques et l'audit.
     * 
     * @param type le type de mouvement (OUTGOING ou INCOMING)
     * @return une liste de mouvements de ce type
     */
    @Query("select sm from StockMovement sm " +
           "where sm.type = :type " +
           "order by sm.movementDate desc")
    List<StockMovement> findByType(StockMovementType type);
}
