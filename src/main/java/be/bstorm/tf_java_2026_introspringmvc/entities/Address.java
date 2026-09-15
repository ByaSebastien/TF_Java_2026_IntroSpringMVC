package be.bstorm.tf_java_2026_introspringmvc.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Address {

    @Getter @Setter
    @Column(length = 100, nullable = false)
    private String street;

    @Getter @Setter
    @Column(length = 10, nullable = false)
    private String number;

    @Getter @Setter
    @Column(length = 4, nullable = false)
    private String postalCode;

    @Getter @Setter
    @Column(length = 100, nullable = false)
    private String city;
}
