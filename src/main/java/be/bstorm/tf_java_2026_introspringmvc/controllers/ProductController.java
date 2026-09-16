package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.models.ProductFilter;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductForm;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductIndexDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    public String index(
            @ModelAttribute ProductFilter filter,
            Model model
    ) {

        List<Product> products = productRepository.findWithFilter(filter.name(),filter.minPrice(),filter.maxPrice(),filter.categoryId());

        List<ProductIndexDto> dtos = products.stream()
                        .map(p -> ProductIndexDto.fromEntity(p))
                        .toList();

        List<CategoryDto> categories = categoryRepository.findAll().stream()
                        .map(c -> CategoryDto.fromEntity(c))
                        .toList();

        model.addAttribute("products", dtos);
        model.addAttribute("categories", categories);
        model.addAttribute("filter", filter);

        return "product/index";
    }

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/create")
    public String create(
            Model model
    ) {
        model.addAttribute("product", new ProductForm());

        List<CategoryDto> categories = categoryRepository.findAll().stream()
                        .map(c -> CategoryDto.fromEntity(c))
                        .toList();

        model.addAttribute("categories", categories);

        return "product/create";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public String create(
            @ModelAttribute(name = "product") ProductForm product,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("product", product);

            List<CategoryDto> categories = categoryRepository.findAll().stream()
                            .map(c -> CategoryDto.fromEntity(c))
                            .toList();

            model.addAttribute("categories", categories);

            return "product/create";
        }

        Product newProduct = product.toEntity();

        Category category = categoryRepository.findById(newProduct.getCategoryId())
                .orElseThrow();

        newProduct.setCategory(category);

        productRepository.save(newProduct);

        return "redirect:/product";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            Model model
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow();

        model.addAttribute("productId", id);
        model.addAttribute("product", product);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "product/update";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute(name = "product") Product product,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("product", product);
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "product/update";
        }

        Product existing = productRepository.findById(id)
                .orElseThrow();

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setImageUrl(product.getImageUrl());
        if (!product.getCategoryId().equals(existing.getCategoryId())) {
            Category category = categoryRepository.findById(product.getCategoryId())
                    .orElseThrow();
            existing.setCategory(category);
        }

        productRepository.save(existing);

        return "redirect:/product";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id
    ) {
        if(!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }

        productRepository.deleteById(id);

        return "redirect:/product";
    }
}
