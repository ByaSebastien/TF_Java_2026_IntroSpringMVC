package be.bstorm.tf_java_2026_introspringmvc.initializers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.Stock;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.enums.UserRole;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialiseur de données (seed) pour remplir la base de données au démarrage.
 * Implémente CommandLineRunner, ce qui signifie que sa méthode run() est exécutée
 * automatiquement au démarrage de l'application Spring Boot.
 * 
 * Utile pour :
 * - Créer des données de test en développement
 * - Initialiser des données nécessaires au fonctionnement de l'application
 * 
 * Les données ne sont créées que si la base est vide (count() == 0).
 */
@Component
@RequiredArgsConstructor
public class Seed implements CommandLineRunner {

    /**
     * Repository pour créer des produits.
     * Injecté automatiquement par Spring.
     */
    private final ProductRepository productRepository;

    /**
     * Repository pour créer des catégories.
     * Injecté automatiquement par Spring.
     */
    private final CategoryRepository categoryRepository;

    /**
     * Repository pour créer des utilisateurs.
     * Injecté automatiquement par Spring.
     */
    private final UserRepository userRepository;

    /**
     * Encodeur pour chiffrer les mots de passe en BCrypt.
     * Jamais stocker les mots de passe en clair en base de données !
     * Injecté automatiquement par Spring.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Méthode appelée automatiquement au démarrage de l'application.
     * Crée les données initiales si la base est vide.
     * 
     * @param args arguments de ligne de commande (pas utilisés ici)
     * @throws Exception si une erreur survient
     */
    @Override
    public void run(String... args) throws Exception {

        // Créer les catégories et produits seulement si la base est vide
        if(productRepository.count() == 0){

            // 1. Créer les catégories
            Category sport = new Category("Sport");
            Category jeux = new Category("Jeux");
            Category art = new Category("Art");

            // 2. Sauvegarder les catégories et récupérer les versions avec ID
            sport = categoryRepository.save(sport);
            jeux = categoryRepository.save(jeux);
            art = categoryRepository.save(art);

            // 3. Créer les produits avec leurs stocks
            List<Product> products = List.of(
                    new Product(
                            "Gants de boxe",
                            "Super gants de boxes",
                            139.99,
                            "https://contents.mediadecathlon.com/p2680600/k$0708d8ee802fc30cc8dbb94e878713cb/picture.jpg?format=auto&f=640x0",
                            sport,
                            new Stock(10, 2)  // 10 en stock, alerte à 2
                    ),
                    new Product(
                            "Onimusha",
                            "Its the GOAT",
                            99.99,
                            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRykiP3_b7hOu1rxwnLWD-m7OvTFEwRCd3g9WP_DvvZmpx8WUn3LU7FV7Q&s=10",
                            jeux,
                            new Stock(50,10)  // 50 en stock, alerte à 10
                    ),
                    new Product(
                            "L'art de la guerre",
                            "Sun Tzu",
                            19.99,
                            "https://m.media-amazon.com/images/I/71KBEeVZ0XL._SL1499_.jpg",
                            art,
                            new Stock(20,5)   // 20 en stock, alerte à 5
                    )
            );

            // 4. Sauvegarder tous les produits
            productRepository.saveAll(products);

        }

        // Créer les utilisateurs seulement si la base est vide
        if(userRepository.count() == 0){

            // 1. Créer un mot de passe chiffré en BCrypt
            String password = passwordEncoder.encode("Test1234=");

            // 2. Créer un utilisateur administrateur
            User admin = new User(
                    "Seb",
                    password,
                    UserRole.ADMIN
            );

            // 3. Créer un utilisateur normal
            User user = new User(
                    "Jean",
                    password,
                    UserRole.USER
            );

            // 4. Sauvegarder les deux utilisateurs
            userRepository.saveAll(List.of(admin,user));
        }

    }
}
