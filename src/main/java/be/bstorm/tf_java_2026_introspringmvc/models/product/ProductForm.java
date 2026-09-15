package be.bstorm.tf_java_2026_introspringmvc.models.product;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ProductForm {

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private double price;

    @Size(max = 255)
    private String description;

    @Size(max = 500)
    private String imageUrl;

    @NotNull
    @Min(1)
    private Long categoryId;

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
