package be.bstorm.tf_java_2026_introspringmvc.entities;

import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente une commande confirmée passée par un utilisateur.
 * 
 * Cycle de vie :
 * - PENDING : En attente de validation par un magasinier
 * - SHIPPED : Validée et expédiée (stocks décrémentés)
 * - CANCELLED : Annulée (raison : indisponibilité partielle ou totale)
 * 
 * La commande contient une adresse de livraison et les produits achetés.
 * Elle peut être transformée en deux commandes si la disponibilité est partielle.
 */
@Entity
@Table(name = "`order`")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString
public class Order extends BaseOrder{

    /**
     * Statut actuel de la commande.
     * Contrôle le flux de travail et les permissions.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Montant total de la commande (calculé depuis les OrderLines).
     * Snapshot du total au moment de la commande.
     */
    @Column
    private Double totalAmount;

    /**
     * L'utilisateur qui a passé cette commande.
     * Relation plusieurs-à-un : un utilisateur peut passer plusieurs commandes.
     */
    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * L'adresse de livraison pour cette commande.
     * Intégrée directement dans cette entité (pas sa propre table).
     */
    @Getter @Setter
    @Embedded
    private Address address;

    /**
     * Crée une commande avec statut initial.
     * 
     * @param user l'utilisateur qui passe la commande
     * @param address l'adresse de livraison
     */
    public Order(User user, Address address) {
        this.user = user;
        this.address = address;
        this.status = OrderStatus.PENDING;
    }

    /**
     * Préalable : vérifie si cette commande peut être validée.
     * Seules les commandes en statut PENDING peuvent être validées.
     * 
     * @return true si la commande peut être validée, false sinon
     */
    public boolean canBeValidated() {
        return this.status == OrderStatus.PENDING;
    }
}

