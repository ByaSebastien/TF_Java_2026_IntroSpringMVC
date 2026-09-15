package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = {"id","name","price"}) @ToString(of = {"id","name","price"})
public class Product extends BaseEntity{

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

    @Getter @Setter
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    @JoinColumn(
            name = "stock_id",
            nullable = false
    )
    private Stock stock;

    public Product(String name, String description, Double price, String imageUrl, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public Product(String name, String description, Double price, String imageUrl, Category category) {
        this(name, description, price, imageUrl, category.getId());
        this.category = category;
    }

    public Product(String name, String description, Double price, String imageUrl, Category category, Stock stock) {
        this(name, description, price, imageUrl, category);
        this.stock = stock;
    }
}
