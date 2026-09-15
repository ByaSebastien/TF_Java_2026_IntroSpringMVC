package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString
// Dans le cas de single table, on peut mettre @DiscriminatorValue("CART") pour avoir une colonne qui indique le type de l'entité
public class Cart extends BaseOrder {

    @Getter @Setter
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
}
