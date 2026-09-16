package be.bstorm.tf_java_2026_introspringmvc.enums;

/**
 * Énumération des rôles d'utilisateur disponibles dans le système.
 * Détermine les permissions et accès aux fonctionnalités de chaque utilisateur.
 */
public enum UserRole {
    /**
     * Administrateur : accès complet au système, peut créer/modifier/supprimer des produits.
     */
    ADMIN,
    
    /**
     * Utilisateur normal : peut consulter les produits, ajouter au panier et passer des commandes.
     */
    USER
}
