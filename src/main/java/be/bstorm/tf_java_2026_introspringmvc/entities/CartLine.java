package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une ligne dans un panier (un produit avec une quantité).
 * Par exemple : "2x Gants de boxe" est une CartLine.
 * Utilise une clé primaire composite (cartId + productId) car une ligne est identifiée
 * de manière unique par son panier et son produit.
 */
@Entity
@EqualsAndHashCode(callSuper = false) @ToString
public class CartLine extends BaseEntity {

    /**
     * La clé primaire composite de cette ligne de panier.
     * Composée de l'ID du panier et de l'ID du produit.
     * Permet de garantir qu'il n'y a qu'une ligne par produit et par panier.
     */
    @EmbeddedId
    private CartLineId id;

    /**
     * La quantité du produit dans ce panier.
     * Par exemple : 3 signifie que l'utilisateur veut 3 unités de ce produit.
     */
    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

    /**
     * Le panier auquel appartient cette ligne.
     * Relation plusieurs-à-un : un panier peut contenir plusieurs lignes.
     */
    @Getter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    @MapsId("cartId")
    private Cart cart;

    /**
     * Le produit de cette ligne de panier.
     * Relation plusieurs-à-un : un produit peut être dans plusieurs paniers.
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
    public CartLine(){
        id = new CartLineId();
    }

    /**
     * Crée une ligne de panier complète.
     * @param quantity la quantité du produit
     * @param cart le panier contenant cette ligne
     * @param product le produit de cette ligne
     */
    public CartLine(int quantity, Cart cart, Product product) {
        this();
        this.quantity = quantity;
        this.cart = cart;
        this.product = product;
        id.setCartId(cart.getId());
        id.setProductId(product.getId());
    }

    /**
     * Défini le panier de cette ligne et met à jour la clé composite.
     * @param cart le nouveau panier
     */
    public void setCart(Cart cart) {
        this.cart = cart;
        id.setCartId(cart.getId());
    }

    /**
     * Défini le produit de cette ligne et met à jour la clé composite.
     * @param product le nouveau produit
     */
    public void setProduct(Product product) {
        this.product = product;
        id.setProductId(product.getId());
    }

    /**
     * Clé primaire composite pour CartLine.
     * Composée de deux champs : cartId et productId.
     * Utilisée pour garantir l'unicité : un même produit ne peut apparaître qu'une fois par panier.
     */
    @Embeddable
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode @ToString
    public static class CartLineId {

        /**
         * L'identifiant du panier.
         */
        @Getter @Setter
        private Long cartId;

        /**
         * L'identifiant du produit.
         */
        @Getter @Setter
        private Long productId;
    }
}
