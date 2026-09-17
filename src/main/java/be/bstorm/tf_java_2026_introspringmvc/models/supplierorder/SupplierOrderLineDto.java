package be.bstorm.tf_java_2026_introspringmvc.models.supplierorder;

import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;

/**
 * DTO représentant une ligne d'une commande fournisseur.
 * 
 * Contient les informations d'une ligne : le produit, la quantité commandée.
 * Utilisé pour afficher les détails d'une commande fournisseur.
 */
public record SupplierOrderLineDto(
        /**
         * L'identifiant unique du produit.
         */
        Long productId,

        /**
         * Le nom du produit.
         */
        String productName,

        /**
         * La quantité commandée au fournisseur.
         */
        Integer quantity
) {

    /**
     * Crée un SupplierOrderLineDto à partir d'une entité SupplierOrderLine.
     * 
     * @param supplierOrderLine l'entité SupplierOrderLine
     * @return un SupplierOrderLineDto prêt à être affiché
     */
    public static SupplierOrderLineDto fromSupplierOrderLine(SupplierOrderLine supplierOrderLine) {
        return new SupplierOrderLineDto(
                supplierOrderLine.getProduct().getId(),
                supplierOrderLine.getProduct().getName(),
                supplierOrderLine.getQuantity()
        );
    }
}
