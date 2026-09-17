package be.bstorm.tf_java_2026_introspringmvc.models.product;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;

public record ProductDetailsDto(
        Long id,
        String name,
        String description,
        double price,
        String imageUrl,
        CategoryDto category
) {

    public static ProductDetailsDto fromProduct(Product p)  {

        return new ProductDetailsDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryDto.fromEntity(p.getCategory())
        );
    }
}
