package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Classe de base abstraite pour les commandes (Cart et Order).
 * Utilise une stratégie d'héritage "TABLE_PER_CLASS" : chaque classe fille (Cart, Order) 
 * possède sa propre table en base de données avec tous les champs de la classe parent.
 * Cela permet de distinguer facilement les paniers des commandes.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public abstract class BaseOrder extends BaseEntity {

    /**
     * Identifiant unique et auto-généré de la commande.
     */
    @Getter
    @Id @GeneratedValue
    private Long id;

    // Les champs spécifiques aux paniers et commandes sont définis dans les classes enfants
}
