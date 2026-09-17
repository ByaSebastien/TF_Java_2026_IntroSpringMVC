package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des lignes de commande fournisseur en base de données.
 * 
 * Responsabilités :
 * - Opérations CRUD sur les lignes
 * - Requêtes pour récupérer les lignes d'une commande fournisseur
 */
@Repository
public interface SupplierOrderLineRepository extends JpaRepository<SupplierOrderLine, SupplierOrderLine.SupplierOrderLineId> {

    /**
     * Cherche toutes les lignes d'une commande fournisseur spécifique.
     * Utilisé pour afficher les détails et éditer une commande fournisseur.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @return une liste de lignes appartenant à cette commande
     */
    @Query("select sol from SupplierOrderLine sol where sol.supplierOrder.id = :supplierOrderId")
    List<SupplierOrderLine> findBySupplierOrderId(Long supplierOrderId);

    /**
     * Cherche toutes les lignes d'une commande fournisseur, jointurées avec produit et commande.
     * Optimisée pour éviter les N+1 queries.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @return une liste de lignes avec données complètes
     */
    @Query("select sol from SupplierOrderLine sol " +
           "join fetch sol.product " +
           "join fetch sol.supplierOrder " +
           "where sol.supplierOrder.id = :supplierOrderId")
    List<SupplierOrderLine> findBySupplierOrderIdWithFetch(Long supplierOrderId);

    /**
     * Supprime toutes les lignes d'une commande fournisseur.
     * Utilisé lors de la suppression d'une commande.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @return le nombre de lignes supprimées
     */
    @Modifying
    @Query("delete from SupplierOrderLine sol where sol.supplierOrder.id = :supplierOrderId")
    int deleteBySupplierOrderId(Long supplierOrderId);

    /**
     * Cherche une ligne spécifique dans une commande fournisseur par produit.
     * Utilisé pour éditer la quantité d'une ligne.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param productId l'ID du produit
     * @return un Optional contenant la SupplierOrderLine si trouvée
     */
    @Query("select sol from SupplierOrderLine sol " +
           "where sol.supplierOrder.id = :supplierOrderId and sol.product.id = :productId")
    java.util.Optional<SupplierOrderLine> findBySupplierOrderAndProduct(Long supplierOrderId, Long productId);

    /**
     * Supprime une ligne spécifique d'une commande fournisseur.
     * Utilisé par le chef de rayon pour retirer un produit de sa commande DRAFT.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param productId l'ID du produit à supprimer
     * @return le nombre de lignes supprimées
     */
    @Modifying
    @Query("delete from SupplierOrderLine sol " +
           "where sol.supplierOrder.id = :supplierOrderId and sol.product.id = :productId")
    int deleteBySupplierOrderAndProduct(Long supplierOrderId, Long productId);
}
