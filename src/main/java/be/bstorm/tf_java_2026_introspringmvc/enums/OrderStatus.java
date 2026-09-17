package be.bstorm.tf_java_2026_introspringmvc.enums;

/**
 * Énumération des statuts possibles pour une commande client.
 */
public enum OrderStatus {
    PENDING,    // En attente de validation magasinier
    SHIPPED,    // Expédiée, stocks décrémentés
    CANCELLED   // Annulée
}
