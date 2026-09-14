package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

//    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
//        this.productRepository = productRepository;
//        this.categoryRepository = categoryRepository;
//    }

//    @GetMapping
//    public String index(
//            @ModelAttribute ProductFilter filter,
//            Model model
//    ) {
//
//        List<Product> products = FakeDb.products.stream().filter(
//                p ->
//                        (filter.name() == null || p.getName().contains(filter.name())) &&
//                                (filter.minPrice() == null || p.getPrice() >= filter.minPrice()) &&
//                                (filter.maxPrice() == null || p.getPrice() <= filter.maxPrice()) &&
//                                (filter.categoryId() == null || p.getCategory().getId().equals(filter.categoryId()))
//        ).toList();
//
//        List<Category> categories = FakeDb.categories;
//
//        model.addAttribute("products", products);
//        model.addAttribute("categories", categories);
//
//        return "product/index";
//    }

    @GetMapping("/{id}")
    public String details(
            @PathVariable Long id,
            Model model
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow();

        model.addAttribute("product", product);

        return "product/details";
    }

    @GetMapping("/create")
    public String create(
            Model model
    ) {
        model.addAttribute("product", new Product());
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "product/create";
    }

    @PostMapping("/create")
    public String create(
            @ModelAttribute(name = "product") Product product,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("product", product);
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "product/create";
        }

        Category category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow();

        product.setCategory(category);

        productRepository.save(product);

        return "redirect:/product";
    }

//    @GetMapping("/update/{id}")
//    public String update(
//            @PathVariable Long id,
//            Model model
//    ) {
//
//        Product product = FakeDb.products.stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst()
//                .orElseThrow();
//
//        model.addAttribute("productId", id);
//        model.addAttribute("product", product);
//        model.addAttribute("categories", FakeDb.categories);
//
//        return "product/update";
//    }
//
//    @PostMapping("/update/{id}")
//    public String update(
//            @PathVariable Long id,
//            @Valid @ModelAttribute(name = "product") Product product,
//            BindingResult bindingResult,
//            Model model
//    ) {
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("productId", id);
//            model.addAttribute("product", product);
//            model.addAttribute("categories", FakeDb.categories);
//            return "product/update";
//        }
//
//        Product existing = FakeDb.products.stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst()
//                .orElseThrow();
//
//        existing.setName(product.getName());
//        existing.setDescription(product.getDescription());
//        existing.setPrice(product.getPrice());
//        existing.setImageUrl(product.getImageUrl());
//        if (!product.getCategoryId().equals(existing.getCategoryId())) {
//            Category category = FakeDb.categories.stream()
//                    .filter(c -> c.getId().equals(product.getCategoryId()))
//                    .findFirst().orElseThrow();
//            existing.setCategory(category);
//        }
//
//        return "redirect:/product";
//    }
//
//    @PostMapping("/delete/{id}")
//    public String delete(
//            @PathVariable Long id
//    ) {
//        Product product = FakeDb.products.stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst().orElseThrow();
//
//        FakeDb.products.remove(product);
//
//        return "redirect:/product";
//    }
}
