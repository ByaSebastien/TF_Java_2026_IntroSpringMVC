package be.bstorm.tf_java_2026_introspringmvc.models.product;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;

public record ProductIndexDto(
        Long id,
        String name,
        double price,
        String imageUrl,
        CategoryDto category
) {

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
