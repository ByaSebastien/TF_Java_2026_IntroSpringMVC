package be.bstorm.tf_java_2026_introspringmvc.entities;

import be.bstorm.tf_java_2026_introspringmvc.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente un mouvement de stock (entrée ou sortie).
 * Permet de tracer l'historique complet des mouvements de chaque produit.
 * 
 * Types de mouvements :
 * - OUTGOING : Sortie de stock (suite à une commande client expédiée)
 * - INCOMING : Entrée en stock (suite à une réception fournisseur)
 * 
 * Cette entité est optionnelle mais très utile pour l'audit et la traçabilité.
 */
@Entity
@Table(name = "stock_movement")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class StockMovement extends BaseEntity {

    /**
     * Identifiant unique et auto-généré du mouvement de stock.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type du mouvement de stock (sortie ou entrée).
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockMovementType type;

    /**
     * Quantité du mouvement.
     * Toujours positive, peu importe le type (OUTGOING ou INCOMING).
     * Doit être > 0.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Date et heure du mouvement.
     * Utilisée pour tracer la chronologie des mouvements.
     */
    @Column(nullable = false)
    private LocalDateTime movementDate;

    /**
     * Le produit concerné par ce mouvement de stock.
     * Chaque mouvement affecte un et un seul produit.
     */
    @Getter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    /**
     * La commande client d'où provient ce mouvement (si type = OUTGOING).
     * Nullable, car un mouvement INCOMING ne sera pas lié à une Order.
     * Permet de tracer : "Ce stock a été sorti pour la commande #1001".
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * La commande fournisseur d'où provient ce mouvement (si type = INCOMING).
     * Nullable, car un mouvement OUTGOING ne sera pas lié à une SupplierOrder.
     * Permet de tracer : "Ce stock a été reçu de la commande fournisseur #SO-001".
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(name = "supplier_order_id")
    private SupplierOrder supplierOrder;

    /**
     * Note ou description du mouvement.
     * Par exemple : "Réception partielle : 8/10 attendus"
     */
    @Column(length = 500)
    private String notes;

    /**
     * Crée un mouvement de stock complet.
     * 
     * @param type le type de mouvement (OUTGOING ou INCOMING)
     * @param quantity la quantité impactée
     * @param movementDate la date du mouvement
     * @param product le produit affecté
     */
    public StockMovement(StockMovementType type, int quantity, LocalDateTime movementDate, Product product) {
        this.type = type;
        this.quantity = quantity;
        this.movementDate = movementDate;
        this.product = product;
    }
}
