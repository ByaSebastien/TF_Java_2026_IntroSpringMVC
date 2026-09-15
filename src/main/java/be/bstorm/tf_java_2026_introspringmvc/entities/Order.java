package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString
public class Order extends BaseOrder{

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Getter @Setter
    @Embedded
    private Address address;
}
