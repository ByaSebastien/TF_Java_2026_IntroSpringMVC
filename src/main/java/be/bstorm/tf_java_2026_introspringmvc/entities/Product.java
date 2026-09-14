package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Product {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Getter @Setter
    @Column()
    private String description;

    @Getter @Setter
    @Column(nullable = false)
    private Double price;

    @Getter @Setter
    @Column(length = 500)
    private String imageUrl;

    @Getter @Setter
    @Column(name = "category_id", nullable = false, insertable = false, updatable = false)
    private Long categoryId;

    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.EAGER,
            cascade = { CascadeType.MERGE }
    )
    @JoinColumn(
            nullable = false,
            name = "category_id"
    )
    private Category category;

    public Product(String name, String description, Double price, String imageUrl, Category category) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
