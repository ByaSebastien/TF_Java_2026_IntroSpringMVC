package be.bstorm.tf_java_2026_introspringmvc.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"wishlist", "password"}) @ToString(exclude = {"wishlist", "password"})
public class User extends BaseEntity{

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Getter @Setter
    @Column(nullable = false)
    private String password;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.MERGE }
    )
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> wishlist = new HashSet<>();

    public User(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public Set<Product> getWishlist() {
        return Set.copyOf(wishlist);
    }

    public void addToWishlist(Product product){
        wishlist.add(product);
    }

    public void removeFromWishlist(Product product){
        wishlist.remove(product);
    }
}
