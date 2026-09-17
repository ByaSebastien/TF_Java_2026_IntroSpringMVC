package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des commandes fournisseur en base de données.
 * 
 * Responsabilités :
 * - Opérations CRUD sur les commandes fournisseur
 * - Requêtes de filtrage par statut
 * - Recherche de commandes en DRAFT (chef de rayon) ou ORDERED (magasinier)
 */
@Repository
public interface SupplierOrderRepository extends JpaRepository<SupplierOrder, Long> {

    /**
     * Cherche toutes les commandes fournisseur avec un statut spécifique.
     * 
     * @param status le statut recherché
     * @return une liste de commandes avec ce statut
     */
    @Query("select so from SupplierOrder so where so.status = :status order by so.createdAt desc")
    List<SupplierOrder> findByStatus(SupplierOrderStatus status);

    /**
     * Cherche toutes les commandes fournisseur en statut DRAFT.
     * Utilisé par le chef de rayon pour éditer les commandes.
     * 
     * @return une liste de commandes en brouillon
     */
    @Query("select so from SupplierOrder so where so.status = 'DRAFT' order by so.createdAt desc")
    List<SupplierOrder> findDraftOrders();

    /**
     * Cherche toutes les commandes fournisseur en statut ORDERED.
     * Utilisé par le magasinier pour valider les réceptions.
     * 
     * @return une liste de commandes passées (à réceptionner)
     */
    @Query("select so from SupplierOrder so where so.status = 'ORDERED' order by so.createdAt desc")
    List<SupplierOrder> findOrderedOrders();

    /**
     * Cherche toutes les commandes fournisseur en statut RECEIVED.
     * Utilisé pour l'historique et le reporting.
     * 
     * @return une liste de commandes reçues
     */
    @Query("select so from SupplierOrder so where so.status = 'RECEIVED' order by so.createdAt desc")
    List<SupplierOrder> findReceivedOrders();
}
