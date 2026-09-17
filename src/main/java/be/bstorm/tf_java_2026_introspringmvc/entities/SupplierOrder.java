package be.bstorm.tf_java_2026_introspringmvc.entities;

import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente une commande passée auprès d'un fournisseur.
 * 
 * Cycle de vie :
 * - DRAFT : En cours de préparation par le chef de rayon (quantités modifiables)
 * - ORDERED : Commandée, envoyée au fournisseur (immuable)
 * - RECEIVED : Réception validée, stocks mises à jour
 * - CANCELLED : Annulée (raison : réception partielle, erreur, etc.)
 * 
 * Les SupplierOrder sont créées automatiquement quand un stock tombe sous son seuil.
 */
@Entity
@Table(name = "supplier_order")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class SupplierOrder extends BaseEntity {

    /**
     * Identifiant unique et auto-généré de la commande fournisseur.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Statut actuel de la commande fournisseur.
     * Contrôle le flux de travail et les permissions d'édition.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SupplierOrderStatus status = SupplierOrderStatus.DRAFT;

    /**
     * Montant estimé ou réel de la commande.
     * Calculé à partir des lignes de commande.
     */
    @Column
    private Double totalAmount;

    /**
     * Raison ou description de la commande fournisseur.
     * Par exemple : "Stock produit A trop bas" ou note libre.
     */
    @Column(length = 500)
    private String description;

    /**
     * Préalable : vérifie si cette commande peut être modifiée.
     * Une SupplierOrder en statut ORDERED est immuable.
     * 
     * @return true si la commande est modifiable (statut DRAFT), false sinon
     */
    public boolean isEditable() {
        return this.status == SupplierOrderStatus.DRAFT;
    }

    /**
     * Préalable : vérifie si cette commande peut être soumise.
     * Une SupplierOrder ne peut être soumise que si elle est en DRAFT.
     * 
     * @return true si la commande peut être soumise, false sinon
     */
    public boolean canBeSubmitted() {
        return this.status == SupplierOrderStatus.DRAFT;
    }

    /**
     * Préalable : vérifie si cette commande peut être reçue.
     * Une SupplierOrder ne peut être reçue que si elle est en statut ORDERED.
     * 
     * @return true si la commande peut être reçue, false sinon
     */
    public boolean canBeReceived() {
        return this.status == SupplierOrderStatus.ORDERED;
    }
}
