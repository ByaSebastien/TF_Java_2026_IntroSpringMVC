package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une ligne dans une commande client (un produit avec sa quantité).
 * Par exemple : "2x Gants de boxe à 15€" est une OrderLine.
 * 
 * Utilise une clé primaire composite (orderId + productId) pour garantir qu'un produit
 * n'apparaît qu'une seule fois par commande.
 * 
 * Contrairement aux CartLine, les OrderLine conservent un snapshot du prix au moment
 * de la commande (si jamais le produit change de prix, les commandes anciennes gardent le bon prix).
 */
@Entity
@Table(name = "order_line")
@EqualsAndHashCode(callSuper = false) @ToString
@AllArgsConstructor
public class OrderLine extends BaseEntity {

    /**
     * Clé primaire composite de cette ligne de commande.
     * Composée de l'ID de la commande et de l'ID du produit.
     * Permet de garantir qu'il n'y a qu'une ligne par produit et par commande.
     */
    @EmbeddedId
    private OrderLineId id;

    /**
     * La quantité du produit demandée dans cette commande.
     * Par exemple : 3 signifie que le client veut 3 unités de ce produit.
     */
    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

    /**
     * Prix unitaire du produit au moment où la commande a été créée (snapshot).
     * Permet de conserver l'historique des prix même si le produit change de prix plus tard.
     */
    @Getter @Setter
    @Column(nullable = false)
    private Double priceSnapshot;

    /**
     * La commande à laquelle appartient cette ligne.
     * Relation plusieurs-à-un : une commande peut contenir plusieurs lignes.
     */
    @Getter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    @MapsId("orderId")
    private Order order;

    /**
     * Le produit de cette ligne de commande.
     * Relation plusieurs-à-un : un produit peut être dans plusieurs commandes.
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
    public OrderLine() {
        id = new OrderLineId();
    }

    /**
     * Crée une ligne de commande complète.
     * 
     * @param quantity la quantité du produit
     * @param priceSnapshot le prix du produit au moment de la commande
     * @param order la commande contenant cette ligne
     * @param product le produit de cette ligne
     */
    public OrderLine(int quantity, Double priceSnapshot, Order order, Product product) {
        this();
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
        this.order = order;
        this.product = product;
        id.setOrderId(order.getId());
        id.setProductId(product.getId());
    }

    /**
     * Définit la commande de cette ligne et met à jour la clé composite.
     * 
     * @param order la nouvelle commande
     */
    public void setOrder(Order order) {
        this.order = order;
        id.setOrderId(order.getId());
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
     * Calcule le montant total de cette ligne : quantity × priceSnapshot
     * 
     * @return le total de la ligne
     */
    public Double getLineTotal() {
        return quantity * priceSnapshot;
    }

    /**
     * Clé primaire composite pour OrderLine.
     * Composée de deux champs : orderId et productId.
     * Utilisée pour garantir l'unicité : un même produit ne peut apparaître qu'une fois par commande.
     */
    @Embeddable
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode @ToString
    public static class OrderLineId {

        /**
         * L'identifiant de la commande.
         */
        @Getter @Setter
        private Long orderId;

        /**
         * L'identifiant du produit.
         */
        @Getter @Setter
        private Long productId;
    }
}
