package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une commande confirmée passée par un utilisateur.
 * La commande contient une adresse de livraison et les produits achetés.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString
public class Order extends BaseOrder{

    /**
     * L'utilisateur qui a passé cette commande.
     * Relation plusieurs-à-un : un utilisateur peut passer plusieurs commandes.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * L'adresse de livraison pour cette commande.
     * Intégrée directement dans cette entité (pas sa propre table).
     */
    @Getter @Setter
    @Embedded
    private Address address;
}
