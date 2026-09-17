package be.bstorm.tf_java_2026_introspringmvc.initializers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.Stock;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.entities.OrderLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.StockMovement;
import be.bstorm.tf_java_2026_introspringmvc.enums.UserRole;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.enums.StockMovementType;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
     * Repository pour créer des paniers.
     * Injecté automatiquement par Spring.
     */
    private final CartRepository cartRepository;

    /**
     * Repository pour créer des lignes de panier.
     * Injecté automatiquement par Spring.
     */
    private final CartLineRepository cartLineRepository;

    /**
     * Repository pour créer des commandes client.
     * Injecté automatiquement par Spring.
     */
    private final OrderRepository orderRepository;

    /**
     * Repository pour créer des lignes de commande.
     * Injecté automatiquement par Spring.
     */
    private final OrderLineRepository orderLineRepository;

    /**
     * Repository pour créer des commandes fournisseur.
     * Injecté automatiquement par Spring.
     */
    private final SupplierOrderRepository supplierOrderRepository;

    /**
     * Repository pour créer des lignes de commande fournisseur.
     * Injecté automatiquement par Spring.
     */
    private final SupplierOrderLineRepository supplierOrderLineRepository;

    /**
     * Repository pour créer des mouvements de stock.
     * Injecté automatiquement par Spring.
     */
    private final StockMovementRepository stockMovementRepository;

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
                            new Stock(10, 9)  // 10 en stock, alerte à 9
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

            // 2. Créer un utilisateur pour chaque rôle
            List<User> users = List.of(
                    new User("ADMIN", password, UserRole.ADMIN),
                    new User("USER", password, UserRole.USER),
                    new User("ROLE_WAREHOUSEMAN", password, UserRole.ROLE_WAREHOUSEMAN),
                    new User("ROLE_DEPARTMENT_HEAD", password, UserRole.ROLE_DEPARTMENT_HEAD)
            );

            // 3. Sauvegarder les utilisateurs
            userRepository.saveAll(users);
        }

        // === BLOC 1 : Créer Carts + CartLines ===
        if(cartRepository.count() == 0){
            // Récupérer les utilisateurs et produits déjà créés
            User user = userRepository.findUserByUsername("USER").orElseThrow();
            User admin = userRepository.findUserByUsername("ADMIN").orElseThrow();
            
            Product gants = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Gants de boxe"))
                    .findFirst().orElseThrow();
            Product onimusha = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Onimusha"))
                    .findFirst().orElseThrow();
            Product art = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("L'art de la guerre"))
                    .findFirst().orElseThrow();

            // Créer panier pour USER
            Cart userCart = new Cart();
            userCart.setUser(user);
            userCart = cartRepository.save(userCart);

            // Ajouter CartLines : Gants x3, Onimusha x2, Art x1
            CartLine gl1 = new CartLine(3, userCart, gants);
            CartLine cl2 = new CartLine(2, userCart, onimusha);
            CartLine cl3 = new CartLine(1, userCart, art);
            cartLineRepository.saveAll(List.of(gl1, cl2, cl3));

            // Créer panier vide pour ADMIN
            Cart adminCart = new Cart();
            adminCart.setUser(admin);
            cartRepository.save(adminCart);
        }

        // === BLOC 2 : Créer Orders + OrderLines + StockMovements (OUTGOING) ===
        if(orderRepository.count() == 0){
            User user = userRepository.findUserByUsername("USER").orElseThrow();
            
            Product gants = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Gants de boxe"))
                    .findFirst().orElseThrow();
            Product onimusha = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Onimusha"))
                    .findFirst().orElseThrow();
            Product art = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("L'art de la guerre"))
                    .findFirst().orElseThrow();

            // Commande 1 : PENDING
            Order order1 = new Order();
            order1.setUser(user);
            order1.setStatus(OrderStatus.PENDING);
            order1.setCreatedAt(LocalDateTime.now());
            order1 = orderRepository.save(order1);

            // OrderLines pour Commande 1
            OrderLine ol1 = new OrderLine(3, gants.getPrice(), order1, gants);
            OrderLine ol2 = new OrderLine(2, onimusha.getPrice(), order1, onimusha);
            OrderLine ol3 = new OrderLine(1, art.getPrice(), order1, art);
            orderLineRepository.saveAll(List.of(ol1, ol2, ol3));

            // Calcul total
            double total = 3 * gants.getPrice() + 2 * onimusha.getPrice() + art.getPrice();
            order1.setTotalAmount(total);
            orderRepository.save(order1);

            // Commande 2 : SHIPPED (déjà expédiée - crée avec stock décrémenté)
            // Crée "Ballon de foot" si n'existe pas
            Product ballon = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Ballon de foot"))
                    .findFirst().orElse(null);
            
            if(ballon == null){
                Category sport = categoryRepository.findAll().stream()
                        .filter(c -> c.getName().equals("Sport"))
                        .findFirst().orElseThrow();
                ballon = new Product("Ballon de foot", "Ballon de qualité", 25.00,
                        "https://www.voetbalshop.be/media/catalog/product/cache/d81c8dc66c69ceb69419c2e7e72e896d/3/3/335075_adidas-wk-2026-trionda-league-voetbal-wit-blauw-rood-groen_1.jpg", sport, new Stock(3, 5));
                ballon = productRepository.save(ballon);
            }

            Order order2 = new Order();
            order2.setUser(user);
            order2.setStatus(OrderStatus.SHIPPED);
            order2.setCreatedAt(LocalDateTime.now().minusDays(1));
            order2 = orderRepository.save(order2);

            OrderLine ol4 = new OrderLine(2, ballon.getPrice(), order2, ballon);
            orderLineRepository.save(ol4);
            order2.setTotalAmount(2 * ballon.getPrice());
            orderRepository.save(order2);

            // Créer StockMovements OUTGOING pour Commande 2 (déjà expédiée)
            StockMovement sm1 = new StockMovement();
            sm1.setProduct(ballon);
            sm1.setQuantity(2);
            sm1.setType(StockMovementType.OUTGOING);
            sm1.setOrder(order2);
            sm1.setMovementDate(LocalDateTime.now().minusDays(1));
            sm1.setCreatedAt(LocalDateTime.now().minusDays(1));
            stockMovementRepository.save(sm1);

            // Décrémenter stock du ballon (simuler l'expédition)
            ballon.getStock().setQuantity(1);  // 3 - 2 = 1
            productRepository.save(ballon);
        }

        // === BLOC 3 : Créer SupplierOrders + SupplierOrderLines ===
        if(supplierOrderRepository.count() == 0){
            Product gants = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Gants de boxe"))
                    .findFirst().orElseThrow();
            Product ballon = productRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Ballon de foot"))
                    .findFirst().orElse(null);

            // SupplierOrder 1 : DRAFT (Gants - décrémentés par la Commande 1 PENDING)
            SupplierOrder so1 = new SupplierOrder();
            so1.setStatus(SupplierOrderStatus.DRAFT);
            so1.setCreatedAt(LocalDateTime.now());
            so1 = supplierOrderRepository.save(so1);

            SupplierOrderLine sol1 = new SupplierOrderLine();
            sol1.setSupplierOrder(so1);
            sol1.setProduct(gants);
            sol1.setQuantity(10);  // seuil=2, donc 2*2=4, on met 10 pour plus de stock
            supplierOrderLineRepository.save(sol1);

            // SupplierOrder 2 : ORDERED (Ballon - déjà commandé)
            SupplierOrder so2 = new SupplierOrder();
            so2.setStatus(SupplierOrderStatus.ORDERED);
            so2.setCreatedAt(LocalDateTime.now().minusDays(2));
            so2 = supplierOrderRepository.save(so2);

            SupplierOrderLine sol2 = new SupplierOrderLine();
            sol2.setSupplierOrder(so2);
            sol2.setProduct(ballon);
            sol2.setQuantity(10);  // seuil=5, donc 5*2=10
            supplierOrderLineRepository.save(sol2);
        }

    }
}
