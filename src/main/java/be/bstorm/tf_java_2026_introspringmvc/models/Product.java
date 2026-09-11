package be.bstorm.tf_java_2026_introspringmvc.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@EqualsAndHashCode @ToString
public class Product {

    public static Long nextId = 1L;

    @Getter
    private Long id;

    @Getter @Setter
    @NotBlank(message = "Not blank please!!!")
    @Size(min = 1, max = 20)
    private String name;

    @Getter @Setter
    private String description;

    @Getter @Setter
    @NotNull
    @Min(0)
    private Double price;

    @Getter @Setter
    @NotBlank
    private String imageUrl;

    @Getter @Setter
    @NotNull
    private Long categoryId;

    @Getter @Setter
    private Category category;

    public Product() {
    }

    public Product(String name, String description, Double price, String imageUrl, Long categoryId, Category category) {
        this.id = nextId++;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.category = category;
    }

    public void takeId(){
        this.id = nextId + 1;
    }
}
