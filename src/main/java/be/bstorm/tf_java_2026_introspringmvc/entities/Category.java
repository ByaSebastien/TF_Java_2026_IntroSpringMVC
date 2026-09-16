package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une catégorie de produits (ex: Sport, Jeux, Art).
 * Permet d'organiser les produits en groupes logiques pour une meilleure navigation.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class Category extends BaseEntity{

    /**
     * Identifiant unique et auto-généré de la catégorie.
     */
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom unique de la catégorie.
     * Doit contenir entre 2 et 50 caractères et doit être unique dans la base de données.
     * Par exemple : "Sport", "Électronique", "Vêtements".
     */
    @Getter @Setter
    @Column(
            unique = true,
            nullable = false,
            length = 50,
            columnDefinition = "VARCHAR(50) NOT NULL UNIQUE CHECK (LENGTH(name) >= 2 AND LENGTH(name) <= 50)"
    )
    private String name;

    /**
     * Crée une nouvelle catégorie avec le nom spécifié.
     * @param name le nom de la catégorie
     */
    public Category(String name) {
        this();
        this.name = name;
    }
}
