package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.models.ProductFilter;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductDetailsDto;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductForm;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductIndexDto;
import be.bstorm.tf_java_2026_introspringmvc.services.ProductService;
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
 * Délègue toute la logique métier au ProductService.
 * 
 * Routes disponibles : /product/...
 */
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    /**
     * Service métier pour gérer les opérations sur les produits.
     * Encapsule la logique et communique avec les repositories.
     * Injecté automatiquement par Spring.
     */
    private final ProductService productService;

    /**
     * Affiche la liste des produits avec possibilité de filtrer.
     * Récupère les produits via le service, les convertit en DTOs
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
        // 1. Récupérer les produits filtrés via le service
        List<ProductIndexDto> products = productService.getFilteredProducts(filter);

        // 2. Récupérer toutes les catégories pour le formulaire de filtrage
        List<CategoryDto> categories = productService.getAllCategoriesAsDto();

        // 3. Passer les données à la vue
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("filter", filter);

        return "product/index";
    }

    /**
     * Affiche les détails complets d'un produit.
     * Récupère le produit par son ID via le service et l'affiche dans une page dédiée.
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
        // Récupérer les détails du produit via le service
        ProductDetailsDto product = productService.getProductDetails(id);

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

        // 2. Récupérer les catégories via le service pour le select du formulaire
        List<CategoryDto> categories = productService.getAllCategoriesAsDto();

        model.addAttribute("categories", categories);

        return "product/create";
    }

    /**
     * Traite la soumission du formulaire de création d'un produit.
     * Valide le formulaire et sauvegarde le nouveau produit en base via le service.
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
            List<CategoryDto> categories = productService.getAllCategoriesAsDto();
            model.addAttribute("categories", categories);

            // Réafficher le formulaire avec les erreurs
            return "product/create";
        }

        // 2. Créer le produit via le service
        productService.createProduct(product.toEntity());

        // 3. Rediriger vers la liste des produits
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
        // 1. Récupérer le produit à modifier via le service
        var product = productService.getProductById(id);

        // 2. Passer le produit et son ID à la vue
        model.addAttribute("productId", id);
        model.addAttribute("product", product);

        // 3. Passer toutes les catégories pour le select via le service
        model.addAttribute("categories", productService.getAllCategories());

        return "product/update";
    }

    /**
     * Traite la soumission du formulaire de modification d'un produit.
     * Valide les données et met à jour le produit en base via le service.
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
            @ModelAttribute(name = "product") ProductForm product,
            BindingResult bindingResult,
            Model model
    ) {
        // 1. Vérifier s'il y a des erreurs de validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("product", product);
            model.addAttribute("categories", productService.getAllCategories());
            return "product/update";
        }

        // 2. Mettre à jour le produit via le service
        productService.updateProduct(id, product.toEntity());

        // 3. Rediriger vers la liste des produits
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
        // Supprimer le produit via le service
        productService.deleteProduct(id);

        // Rediriger vers la liste des produits
        return "redirect:/product";
    }
}
