package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@EqualsAndHashCode(callSuper = false) @ToString
public class CartLine extends BaseEntity {

    @EmbeddedId
    private CartLineId id;

    @Getter @Setter
    @Column(nullable = false)
    private int quantity;

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

    public CartLine(){
        id = new CartLineId();
    }

    public CartLine(int quantity, Cart cart, Product product) {
        this();
        this.quantity = quantity;
        this.cart = cart;
        this.product = product;
        id.setCartId(cart.getId());
        id.setProductId(product.getId());
    }

    public void setCart(Cart cart) {
        this.cart = cart;
        id.setCartId(cart.getId());
    }

    public void setProduct(Product product) {
        this.product = product;
        id.setProductId(product.getId());
    }

    @Embeddable
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode @ToString
    public static class CartLineId {

        @Getter @Setter
        private Long cartId;

        @Getter @Setter
        private Long productId;
    }
}
