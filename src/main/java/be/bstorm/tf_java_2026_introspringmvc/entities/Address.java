package be.bstorm.tf_java_2026_introspringmvc.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Représente l'adresse d'une commande.
 * Utilisée pour envoyer les produits au bon endroit.
 * C'est une classe "embeddable" : elle n'a pas sa propre table en base de données,
 * mais elle est intégrée directement dans les entités qui l'utilisent (comme Order).
 */
@Embeddable
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Address {

    /**
     * Nom de la rue (ex: "Rue de la Paix", "Avenue des Champs").
     */
    @Getter @Setter
    @Column(length = 100, nullable = false)
    private String street;

    /**
     * Numéro de la maison ou du bâtiment (ex: "123", "45A").
     */
    @Getter @Setter
    @Column(length = 10, nullable = false)
    private String number;

    /**
     * Code postal de l'adresse (ex: "75001", "69000").
     */
    @Getter @Setter
    @Column(length = 4, nullable = false)
    private String postalCode;

    /**
     * Ville de l'adresse (ex: "Paris", "Lyon", "Bruxelles").
     */
    @Getter @Setter
    @Column(length = 100, nullable = false)
    private String city;
}
