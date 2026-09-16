package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente le panier d'un utilisateur.
 * Le panier contient les produits que l'utilisateur souhaite acheter avant de passer la commande.
 * Chaque utilisateur n'a qu'un seul panier actif.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString
public class Cart extends BaseOrder {

    /**
     * L'utilisateur propriétaire de ce panier.
     * Relation "un-à-un" : chaque panier appartient à un seul utilisateur.
     */
    @Getter @Setter
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
}
