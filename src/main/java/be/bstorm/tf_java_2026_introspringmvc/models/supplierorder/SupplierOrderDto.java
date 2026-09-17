package be.bstorm.tf_java_2026_introspringmvc.models.supplierorder;

import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour afficher les détails complets d'une commande fournisseur.
 * 
 * Utilisé par le chef de rayon pour gérer les commandes (créer, éditer en DRAFT, soumettre).
 * Utilisé par le magasinier pour recevoir les commandes.
 */
public record SupplierOrderDto(
        /**
         * L'identifiant unique de la commande fournisseur.
         */
        Long supplierOrderId,

        /**
         * Le statut actuel (DRAFT, ORDERED, RECEIVED, CANCELLED).
         */
        SupplierOrderStatus status,

        /**
         * La date de création de la commande.
         */
        LocalDateTime createdAt,

        /**
         * Liste des lignes de cette commande fournisseur.
         */
        List<SupplierOrderLineDto> lines
) {

    /**
     * Crée un SupplierOrderDto à partir d'une entité SupplierOrder et ses lignes.
     * 
     * @param supplierOrder l'entité SupplierOrder
     * @param lines la liste des SupplierOrderLineDto
     * @return un SupplierOrderDto prêt à être affiché
     */
    public static SupplierOrderDto fromSupplierOrder(SupplierOrder supplierOrder, List<SupplierOrderLineDto> lines) {
        return new SupplierOrderDto(
                supplierOrder.getId(),
                supplierOrder.getStatus(),
                supplierOrder.getCreatedAt(),
                lines
        );
    }
}
