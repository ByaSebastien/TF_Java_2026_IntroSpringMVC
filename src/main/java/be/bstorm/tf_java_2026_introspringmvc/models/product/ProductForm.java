package be.bstorm.tf_java_2026_introspringmvc.models.product;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Formulaire pour créer ou modifier un produit.
 * Contient les données soumises par un administrateur via le formulaire HTML.
 * Les annotations @NotBlank, @Min, @Size sont utilisées par Spring Validation
 * pour vérifier que les données sont correctes avant de les sauvegarder.
 */
@Data
@NoArgsConstructor @AllArgsConstructor
public class ProductForm {

    /**
     * Nom du produit.
     * Ne doit pas être vide ou composé que d'espaces.
     */
    @NotBlank
    private String name;

    /**
     * Prix du produit en euros.
     * Doit être supérieur ou égal à 0.
     */
    @NotNull
    @Min(0)
    private double price;

    /**
     * Description du produit.
     * Champ optionnel, limité à 255 caractères.
     */
    @Size(max = 255)
    private String description;

    /**
     * URL de l'image du produit.
     * Champ optionnel, limité à 500 caractères.
     */
    @Size(max = 500)
    private String imageUrl;

    /**
     * L'identifiant de la catégorie du produit.
     * Doit être supérieur ou égal à 1 (une catégorie doit exister).
     */
    @NotNull
    @Min(1)
    private Long categoryId;

    /**
     * Convertit ce formulaire en entité Product.
     * Crée une nouvelle instance Product avec les données du formulaire.
     * @return une entité Product prête à être sauvegardée en base
     */
    public Product toEntity() {
        Product p = new Product(
                name,
                description,
                price,
                imageUrl,
                categoryId
        );

        return p;
    }
}
