package be.bstorm.tf_java_2026_introspringmvc.enums;

/**
 * Énumération des statuts possibles pour une commande fournisseur.
 */
public enum SupplierOrderStatus {
    DRAFT,      // En cours de préparation (modifiable)
    ORDERED,    // Commandée et immuable
    RECEIVED,   // Réceptionnée
    CANCELLED   // Annulée
}
