package be.bstorm.tf_java_2026_introspringmvc.models;

public record ProductFilter(
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId
) {
}
