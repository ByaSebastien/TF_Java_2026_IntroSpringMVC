package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public abstract class BaseEntity {

    @Getter @Setter
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Getter @Setter
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
