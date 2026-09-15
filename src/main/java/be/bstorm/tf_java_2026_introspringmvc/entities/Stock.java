package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class Stock extends BaseEntity{

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

    @Getter @Setter
    @Column(nullable = false)
    private int threshold;

    public Stock(int quantity, int threshold) {
        this.quantity = quantity;
        this.threshold = threshold;
    }
}
