package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
// Dans le cas de single table, on peut mettre @DiscriminatorColumn(name = "type") pour avoir une colonne qui indique le type de l'entité
public abstract class BaseOrder extends BaseEntity {

    @Getter
    @Id @GeneratedValue
    private Long id;

    // Ici les champs communs
}
