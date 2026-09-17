package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une ligne dans une commande fournisseur (un produit avec sa quantité).
 * Par exemple : "20x Gants de boxe" est une SupplierOrderLine.
 * 
 * Utilise une clé primaire composite (supplierOrderId + productId) pour garantir
 * qu'un produit n'apparaît qu'une seule fois par commande fournisseur.
 * 
 * Les quantités peuvent être modifiées par le chef de rayon tant que la commande
 * est en statut DRAFT. Une fois ORDERED, la ligne devient immuable.
 */
@Entity
@Table(name = "supplier_order_line")
@EqualsAndHashCode(callSuper = false) @ToString
@AllArgsConstructor
public class SupplierOrderLine extends BaseEntity {

    /**
     * Clé primaire composite de cette ligne de commande fournisseur.
     * Composée de l'ID de la commande fournisseur et de l'ID du produit.
     * Permet de garantir qu'il n'y a qu'une ligne par produit et par commande.
     */
    @EmbeddedId
    private SupplierOrderLineId id;

    /**
     * La quantité du produit commandée au fournisseur.
     * Par exemple : 20 signifie qu'on commande 20 unités de ce produit.
     * 
     * Cette quantité peut être modifiée par le chef de rayon (si commande en DRAFT)
     * avant de passer la commande.
     */
    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

    /**
     * La commande fournisseur à laquelle appartient cette ligne.
     * Relation plusieurs-à-un : une commande peut contenir plusieurs lignes.
     */
    @Getter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(
            name = "supplier_order_id",
            nullable = false
    )
    @MapsId("supplierOrderId")
    private SupplierOrder supplierOrder;

    /**
     * Le produit de cette ligne de commande fournisseur.
     * Relation plusieurs-à-un : un produit peut être dans plusieurs commandes fournisseur.
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
    @MapsId("productId")
    private Product product;

    /**
     * Constructeur sans paramètres qui initialise la clé composite.
     */
    public SupplierOrderLine() {
        id = new SupplierOrderLineId();
    }

    /**
     * Crée une ligne de commande fournisseur complète.
     * 
     * @param quantity la quantité du produit à commander
     * @param supplierOrder la commande fournisseur contenant cette ligne
     * @param product le produit de cette ligne
     */
    public SupplierOrderLine(int quantity, SupplierOrder supplierOrder, Product product) {
        this();
        this.quantity = quantity;
        this.supplierOrder = supplierOrder;
        this.product = product;
        id.setSupplierOrderId(supplierOrder.getId());
        id.setProductId(product.getId());
    }

    /**
     * Définit la commande fournisseur de cette ligne et met à jour la clé composite.
     * 
     * @param supplierOrder la nouvelle commande fournisseur
     */
    public void setSupplierOrder(SupplierOrder supplierOrder) {
        this.supplierOrder = supplierOrder;
        id.setSupplierOrderId(supplierOrder.getId());
    }

    /**
     * Définit le produit de cette ligne et met à jour la clé composite.
     * 
     * @param product le nouveau produit
     */
    public void setProduct(Product product) {
        this.product = product;
        id.setProductId(product.getId());
    }

    /**
     * Clé primaire composite pour SupplierOrderLine.
     * Composée de deux champs : supplierOrderId et productId.
     * Utilisée pour garantir l'unicité : un même produit ne peut apparaître qu'une fois par commande.
     */
    @Embeddable
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode @ToString
    public static class SupplierOrderLineId {

        /**
         * L'identifiant de la commande fournisseur.
         */
        @Getter @Setter
        private Long supplierOrderId;

        /**
         * L'identifiant du produit.
         */
        @Getter @Setter
        private Long productId;
    }
}
