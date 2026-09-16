package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Classe de base abstraite pour toutes les entités du système.
 * Elle fournit les champs d'audit temporels (création et modification).
 * Permet de tracer quand les données ont été créées et modifiées pour des raisons de debug et de reporting.
 */
@MappedSuperclass
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public abstract class BaseEntity {

    /**
     * Date et heure de création de l'entité.
     * Définie automatiquement par Hibernate lors de la première sauvegarde.
     */
    @Getter @Setter
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Date et heure de la dernière modification de l'entité.
     * Mise à jour automatiquement par Hibernate à chaque sauvegarde.
     */
    @Getter @Setter
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
