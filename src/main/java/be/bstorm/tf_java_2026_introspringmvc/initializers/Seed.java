package be.bstorm.tf_java_2026_introspringmvc.initializers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.Stock;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Seed implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {

        if(productRepository.count() == 0){


            Category sport = new Category("Sport");
            Category jeux = new Category("Jeux");
            Category art = new Category("Art");

            sport = categoryRepository.save(sport);
            jeux = categoryRepository.save(jeux);
            art = categoryRepository.save(art);

            List<Product> products = List.of(
                    new Product(
                            "Gants de boxe",
                            "Super gants de boxes",
                            139.99,
                            "https://contents.mediadecathlon.com/p2680600/k$0708d8ee802fc30cc8dbb94e878713cb/picture.jpg?format=auto&f=640x0",
                            sport,
                            new Stock(10, 2)
                    ),
                    new Product(
                            "Onimusha",
                            "Its the GOAT",
                            99.99,
                            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRykiP3_b7hOu1rxwnLWD-m7OvTFEwRCd3g9WP_DvvZmpx8WUn3LU7FV7Q&s=10",
                            jeux,
                            new Stock(50,10)
                    ),
                    new Product(
                            "L'art de la guerre",
                            "Sun Tzu",
                            19.99,
                            "https://m.media-amazon.com/images/I/71KBEeVZ0XL._SL1499_.jpg",
                            art,
                            new Stock(20,5)
                    )
            );

            productRepository.saveAll(products);

        }

    }
}
