package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class Category extends BaseEntity{

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @Column(
            unique = true,
            nullable = false,
            length = 50,
            columnDefinition = "VARCHAR(50) NOT NULL UNIQUE CHECK (LENGTH(name) >= 2 AND LENGTH(name) <= 50)"
    )
    private String name;

    public Category(String name) {
        this();
        this.name = name;
    }
}
