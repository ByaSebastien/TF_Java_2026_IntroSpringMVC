package be.bstorm.tf_java_2026_introspringmvc.entities;

import be.bstorm.tf_java_2026_introspringmvc.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Représente un utilisateur du système d'e-commerce.
 * Un utilisateur peut se connecter, avoir un panier et une liste de souhaits.
 * Implémente UserDetails de Spring Security pour intégrer l'authentification et l'autorisation.
 */
@Entity
@Table(name = "user_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"wishlist", "password"}) @ToString(exclude = {"wishlist", "password"})
public class User extends BaseEntity implements UserDetails {

    /**
     * Identifiant unique et auto-généré de l'utilisateur.
     */
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom d'utilisateur unique pour l'authentification.
     * Chaque utilisateur doit avoir un nom d'utilisateur différent.
     */
    @Getter @Setter
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * Mot de passe chiffré en BCrypt.
     * N'est jamais stocké en clair pour des raisons de sécurité.
     */
    @Getter @Setter
    @Column(nullable = false)
    private String password;

    /**
     * Rôle de l'utilisateur (ADMIN ou USER).
     * Détermine les permissions et accès aux fonctionnalités protégées.
     */
    @Getter @Setter
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * Liste des produits que l'utilisateur a ajouté à sa liste de souhaits.
     * Relation plusieurs-à-plusieurs pour permettre à plusieurs utilisateurs
     * d'aimer le même produit et vice-versa.
     */
    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.MERGE }
    )
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> wishlist = new HashSet<>();

    /**
     * Crée un nouvel utilisateur avec les paramètres spécifiés.
     * @param username le nom d'utilisateur
     * @param password le mot de passe (doit être chiffré avant sauvegarde)
     * @param role le rôle de l'utilisateur (ADMIN ou USER)
     */
    public User(String username, String password, UserRole role) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Retourne une copie immuable de la liste de souhaits.
     * Cela empêche les modifications externes d'affecter l'état réel.
     * @return une copie de la wishlist
     */
    public Set<Product> getWishlist() {
        return Set.copyOf(wishlist);
    }

    /**
     * Ajoute un produit à la liste de souhaits de l'utilisateur.
     * @param product le produit à ajouter
     */
    public void addToWishlist(Product product){
        wishlist.add(product);
    }

    /**
     * Retire un produit de la liste de souhaits de l'utilisateur.
     * @param product le produit à retirer
     */
    public void removeFromWishlist(Product product){
        wishlist.remove(product);
    }

    /**
     * Retourne les autorités (permissions) de l'utilisateur.
     * Utilisé par Spring Security pour vérifier les droits d'accès.
     * @return une collection contenant le rôle de l'utilisateur comme autorité
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
