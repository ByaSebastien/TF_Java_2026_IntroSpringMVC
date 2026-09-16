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

/**
 * Contrôleur pour gérer l'affichage et les opérations sur les produits.
 * Fonctionnalités :
 * - Lister les produits avec filtres (nom, prix, catégorie)
 * - Afficher les détails d'un produit
 * - Créer, modifier et supprimer des produits (ADMIN uniquement)
 * 
 * Routes disponibles : /product/...
 */
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    /**
     * Repository pour accéder aux produits en base de données.
     * Injecté automatiquement par Spring.
     */
    private final ProductRepository productRepository;

    /**
     * Repository pour accéder aux catégories en base de données.
     * Injecté automatiquement par Spring.
     */
    private final CategoryRepository categoryRepository;

    /**
     * Affiche la liste des produits avec possibilité de filtrer.
     * Récupère les produits selon les filtres spécifiés, les convertit en DTOs
     * et les passe à la vue pour affichage.
     * 
     * @param filter les critères de filtrage (nom, prix min/max, catégorie)
     * @param model l'objet pour passer les données à la vue
     * @return le nom du template à afficher : "product/index"
     */
    @GetMapping
    public String index(
            @ModelAttribute ProductFilter filter,
            Model model
    ) {

        // 1. Récupérer les produits avec les filtres
        List<Product> products = productRepository.findWithFilter(filter.name(),filter.minPrice(),filter.maxPrice(),filter.categoryId());

        // 2. Convertir les entités en DTOs pour la vue
        List<ProductIndexDto> dtos = products.stream()
                        .map(p -> ProductIndexDto.fromEntity(p))
                        .toList();

        // 3. Récupérer toutes les catégories pour le formulaire de filtrage
        List<CategoryDto> categories = categoryRepository.findAll().stream()
                        .map(c -> CategoryDto.fromEntity(c))
                        .toList();

        // 4. Passer les données à la vue
        model.addAttribute("products", dtos);
        model.addAttribute("categories", categories);
        model.addAttribute("filter", filter);

        return "product/index";
    }

    /**
     * Affiche les détails complets d'un produit.
     * Récupère le produit par son ID et l'affiche dans une page dédiée.
     * 
     * @param id l'ID du produit à afficher
     * @param model l'objet pour passer le produit à la vue
     * @return le nom du template à afficher : "product/details"
     */
    @GetMapping("/{id}")
    public String details(
            @PathVariable Long id,
            Model model
    ) {
        // Récupérer le produit (exception si inexistant)
        Product product = productRepository.findById(id)
                .orElseThrow();

        model.addAttribute("product", product);

        return "product/details";
    }

    /**
     * Affiche le formulaire de création d'un produit.
     * Accessible uniquement aux administrateurs.
     * 
     * @param model l'objet pour passer un formulaire vide et les catégories à la vue
     * @return le nom du template à afficher : "product/create"
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/create")
    public String create(
            Model model
    ) {
        // 1. Créer un formulaire vide
        model.addAttribute("product", new ProductForm());

        // 2. Récupérer les catégories pour le select du formulaire
        List<CategoryDto> categories = categoryRepository.findAll().stream()
                        .map(c -> CategoryDto.fromEntity(c))
                        .toList();

        model.addAttribute("categories", categories);

        return "product/create";
    }

    /**
     * Traite la soumission du formulaire de création d'un produit.
     * Valide le formulaire et sauvegarde le nouveau produit en base.
     * 
     * @param product le formulaire soumis par l'utilisateur
     * @param bindingResult l'objet contenant les erreurs de validation
     * @param model l'objet pour passer les données à la vue en cas d'erreur
     * @return redirection vers la liste des produits en cas de succès,
     *         réaffichage du formulaire en cas d'erreur
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public String create(
            @ModelAttribute(name = "product") ProductForm product,
            BindingResult bindingResult,
            Model model
    ) {

        // 1. Vérifier s'il y a des erreurs de validation
        if (bindingResult.hasErrors()) {

            model.addAttribute("product", product);

            List<CategoryDto> categories = categoryRepository.findAll().stream()
                            .map(c -> CategoryDto.fromEntity(c))
                            .toList();

            model.addAttribute("categories", categories);

            // Réafficher le formulaire avec les erreurs
            return "product/create";
        }

        // 2. Convertir le formulaire en entité
        Product newProduct = product.toEntity();

        // 3. Récupérer la catégorie (exception si inexistante)
        Category category = categoryRepository.findById(newProduct.getCategoryId())
                .orElseThrow();

        // 4. Associer la catégorie au produit
        newProduct.setCategory(category);

        // 5. Sauvegarder le produit en base
        productRepository.save(newProduct);

        // 6. Rediriger vers la liste des produits
        return "redirect:/product";
    }

    /**
     * Affiche le formulaire de modification d'un produit existant.
     * Accessible uniquement aux administrateurs.
     * 
     * @param id l'ID du produit à modifier
     * @param model l'objet pour passer le produit et les catégories à la vue
     * @return le nom du template à afficher : "product/update"
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            Model model
    ) {

        // 1. Récupérer le produit à modifier (exception si inexistant)
        Product product = productRepository.findById(id)
                .orElseThrow();

        // 2. Passer le produit et son ID à la vue
        model.addAttribute("productId", id);
        model.addAttribute("product", product);

        // 3. Passer toutes les catégories pour le select
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "product/update";
    }

    /**
     * Traite la soumission du formulaire de modification d'un produit.
     * Valide les données et met à jour le produit en base.
     * 
     * @param id l'ID du produit à modifier
     * @param product les données modifiées du produit
     * @param bindingResult l'objet contenant les erreurs de validation
     * @param model l'objet pour passer les données à la vue en cas d'erreur
     * @return redirection vers la liste des produits en cas de succès,
     *         réaffichage du formulaire en cas d'erreur
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute(name = "product") Product product,
            BindingResult bindingResult,
            Model model
    ) {
        // 1. Vérifier s'il y a des erreurs de validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("product", product);
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "product/update";
        }

        // 2. Récupérer le produit existant depuis la base
        Product existing = productRepository.findById(id)
                .orElseThrow();

        // 3. Mettre à jour les champs modifiables
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setImageUrl(product.getImageUrl());

        // 4. Vérifier si la catégorie a changé
        if (!product.getCategoryId().equals(existing.getCategoryId())) {
            Category category = categoryRepository.findById(product.getCategoryId())
                    .orElseThrow();
            existing.setCategory(category);
        }

        // 5. Sauvegarder les modifications
        productRepository.save(existing);

        // 6. Rediriger vers la liste des produits
        return "redirect:/product";
    }

    /**
     * Supprime un produit existant.
     * Accessible uniquement aux administrateurs.
     * Utilise POST car une suppression doit être une action "dangereuse" (pas cachée en GET).
     * 
     * @param id l'ID du produit à supprimer
     * @return redirection vers la liste des produits
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id
    ) {
        // 1. Vérifier que le produit existe
        if(!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }

        // 2. Supprimer le produit
        productRepository.deleteById(id);

        // 3. Rediriger vers la liste des produits
        return "redirect:/product";
    }
}
