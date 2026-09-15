package be.bstorm.tf_java_2026_introspringmvc.models.category;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;

public record CategoryDto(
        Long id,
        String name
) {

    public static CategoryDto fromEntity(Category c){
        return new CategoryDto(
                c.getId(),
                c.getName()
        );
    }
}
