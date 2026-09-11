package be.bstorm.tf_java_2026_introspringmvc.datas;

import be.bstorm.tf_java_2026_introspringmvc.models.Category;
import be.bstorm.tf_java_2026_introspringmvc.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FakeDb {

    public static List<Product> products = new ArrayList<>();
    public static List<Category> categories = new ArrayList<>();

    static {

        Category category = new Category("Category1");
        Category category2 = new Category("Category2");
        Category category3 = new Category("Category3");

        categories.add(category);
        categories.add(category2);
        categories.add(category3);

        products.add(new Product(
                "Product 1",
                "...",
                42.0,
                "https://img.magnific.com/photos-gratuite/gros-plan-beau-papillon-textures-interessantes-fleur-petale-orange_181624-7640.jpg?semt=ais_hybrid&w=740&q=80",
                1L,
                category
        ));
        products.add(new Product(
                "Product 2",
                "...",
                10.0,
                "https://img.magnific.com/photos-gratuite/gros-plan-beau-papillon-textures-interessantes-fleur-petale-orange_181624-7640.jpg?semt=ais_hybrid&w=740&q=80",
                2L,
                category2
        ));
        products.add(new Product(
                "Product 3",
                "...",
                108.0,
                "https://img.magnific.com/photos-gratuite/gros-plan-beau-papillon-textures-interessantes-fleur-petale-orange_181624-7640.jpg?semt=ais_hybrid&w=740&q=80",
                3L,
                category3
        ));
    }
}
