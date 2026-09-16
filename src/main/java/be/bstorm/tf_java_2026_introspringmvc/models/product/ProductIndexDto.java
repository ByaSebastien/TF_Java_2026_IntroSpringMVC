package be.bstorm.tf_java_2026_introspringmvc.models.product;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;

/**
 * DTO pour afficher un produit dans le catalogue (liste des produits).
 * Contient les informations essentielles : ID, nom, prix, image et catégorie.
 * C'est une version "lightweight" du Product, idéale pour afficher une liste sans charger trop de données.
 *
 * @param id l'identifiant du produit
 * @param name le nom du produit
 * @param price le prix du produit
 * @param imageUrl l'URL de l'image du produit
 * @param category la catégorie du produit (sous forme de DTO)
 */
public record ProductIndexDto(
        Long id,
        String name,
        double price,
        String imageUrl,
        CategoryDto category
) {

    /**
     * Convertit une entité Product en DTO pour l'affichage en liste.
     * Extrait uniquement les champs nécessaires pour la vue catalogue.
     * @param p le produit à convertir
     * @return un DTO contenant les infos publiques du produit
     */
    public static ProductIndexDto fromEntity(Product p)  {

        return new ProductIndexDto(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryDto.fromEntity(p.getCategory())
        );
    }
}
