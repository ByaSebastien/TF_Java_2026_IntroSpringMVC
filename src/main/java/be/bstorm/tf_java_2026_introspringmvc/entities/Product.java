package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente un produit disponible à la vente.
 * Chaque produit appartient à une catégorie, possède un stock et peut être dans plusieurs paniers.
 */
@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = {"id","name","price"}) @ToString(of = {"id","name","price"})
public class Product extends BaseEntity{

    /**
     * Identifiant unique et auto-généré du produit.
     */
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom unique du produit affiché aux clients.
     * Par exemple : "Gants de boxe", "Onimusha", "L'art de la guerre".
     */
    @Getter @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Description détaillée du produit pour aider le client à prendre sa décision d'achat.
     * Champ optionnel.
     */
    @Getter @Setter
    @Column()
    private String description;

    /**
     * Prix du produit en euros.
     * Doit être positif ou nul.
     */
    @Getter @Setter
    @Column(nullable = false)
    private Double price;

    /**
     * URL de l'image du produit à afficher dans le catalogue.
     * Peut être une URL distante vers un serveur d'images.
     */
    @Getter @Setter
    @Column(length = 500)
    private String imageUrl;

    /**
     * Identifiant de la catégorie du produit.
     * Stocké également directement pour optimiser les requêtes.
     */
    @Getter @Setter
    @Column(name = "category_id", nullable = false, insertable = false, updatable = false)
    private Long categoryId;

    /**
     * La catégorie à laquelle appartient ce produit.
     * Relation plusieurs produits vers une catégorie.
     */
    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.EAGER,
            cascade = { CascadeType.MERGE }
    )
    @JoinColumn(
            nullable = false,
            name = "category_id"
    )
    private Category category;

    /**
     * Le stock disponible pour ce produit.
     * Gère la quantité en magasin et le seuil d'alerte.
     */
    @Getter @Setter
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    @JoinColumn(
            name = "stock_id",
            nullable = false
    )
    private Stock stock;

    /**
     * Crée un produit sans stock ni catégorie liée.
     * Utile pour les formulaires avant sauvegarde complète.
     */
    public Product(String name, String description, Double price, String imageUrl, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    /**
     * Crée un produit avec une catégorie associée.
     * @param name le nom du produit
     * @param description la description du produit
     * @param price le prix du produit
     * @param imageUrl l'URL de l'image
     * @param category la catégorie liée au produit
     */
    public Product(String name, String description, Double price, String imageUrl, Category category) {
        this(name, description, price, imageUrl, category.getId());
        this.category = category;
    }

    /**
     * Crée un produit complet avec catégorie et stock.
     * @param name le nom du produit
     * @param description la description du produit
     * @param price le prix du produit
     * @param imageUrl l'URL de l'image
     * @param category la catégorie liée au produit
     * @param stock le stock du produit
     */
    public Product(String name, String description, Double price, String imageUrl, Category category, Stock stock) {
        this(name, description, price, imageUrl, category);
        this.stock = stock;
    }
}
