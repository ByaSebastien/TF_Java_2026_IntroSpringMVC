package be.bstorm.tf_java_2026_introspringmvc.models.category;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;

/**
 * DTO (Data Transfer Object) pour afficher une catégorie dans les templates.
 * Contient uniquement les informations publiques : l'ID et le nom.
 * Les DTOs permettent de ne passer que les données nécessaires au client,
 * sans exposer les entités JPA complètes.
 *
 * @param id l'identifiant de la catégorie
 * @param name le nom de la catégorie
 */
public record CategoryDto(
        Long id,
        String name
) {

    /**
     * Convertit une entité Category en DTO.
     * Utile pour transformer les données de la base de données avant de les envoyer à la vue.
     * @param c la catégorie à convertir
     * @return un DTO contenant les informations de la catégorie
     */
    public static CategoryDto fromEntity(Category c){
        return new CategoryDto(
                c.getId(),
                c.getName()
        );
    }
}
