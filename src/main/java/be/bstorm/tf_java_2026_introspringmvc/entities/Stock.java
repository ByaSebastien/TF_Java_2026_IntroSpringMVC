package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Gère le stock disponible d'un produit.
 * Contient la quantité actuelle en magasin et un seuil d'alerte pour repassage de commande.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class Stock extends BaseEntity{

    /**
     * Identifiant unique et auto-généré du stock.
     */
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quantité actuelle du produit en stock.
     * Doit être supérieur ou égal à 0.
     */
    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

    /**
     * Seuil minimum d'alerte pour ce produit.
     * Quand la quantité tombe sous ce seuil, il faut recommander.
     * Par exemple : threshold = 5 signifie qu'il faut recommander si quantity < 5.
     */
    @Getter @Setter
    @Column(nullable = false)
    private int threshold;

    /**
     * Crée un stock avec une quantité et un seuil.
     * @param quantity la quantité initiale en stock
     * @param threshold le seuil d'alerte
     */
    public Stock(int quantity, int threshold) {
        this.quantity = quantity;
        this.threshold = threshold;
    }
}
