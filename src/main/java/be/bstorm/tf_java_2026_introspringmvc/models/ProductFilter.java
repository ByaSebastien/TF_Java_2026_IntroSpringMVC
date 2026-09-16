package be.bstorm.tf_java_2026_introspringmvc.models;

/**
 * Filtre pour rechercher et affiner la liste des produits.
 * Permet aux utilisateurs de chercher par nom, plage de prix ou catégorie.
 * Les champs optionnels (null) signifient "pas de filtre sur ce critère".
 * 
 * Exemple d'utilisation :
 * - ProductFilter(null, 10.0, 100.0, null) = tous les produits entre 10€ et 100€
 * - ProductFilter("Gants", null, null, 1L) = tous les produits nommés "Gants" de la catégorie 1
 * - ProductFilter(null, null, null, null) = tous les produits (pas de filtre)
 *
 * @param name le nom ou partie du nom du produit à chercher (optionnel)
 * @param minPrice le prix minimum (optionnel)
 * @param maxPrice le prix maximum (optionnel)
 * @param categoryId l'ID de la catégorie (optionnel)
 */
public record ProductFilter(
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId
) {
}
