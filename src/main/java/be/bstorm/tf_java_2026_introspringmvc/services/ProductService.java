package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.models.ProductFilter;
import be.bstorm.tf_java_2026_introspringmvc.models.category.CategoryDto;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductDetailsDto;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductIndexDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CategoryRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier pour gérer les opérations sur les produits.
 * Encapsule la logique applicative et fait le lien entre le contrôleur et les repositories.
 * 
 * Responsabilités :
 * - Récupération et filtrage des produits
 * - Conversion entités -> DTOs
 * - Création, modification et suppression de produits
 * - Gestion des catégories associées aux produits
 */
@Service
@RequiredArgsConstructor
public class ProductService {

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
     * Récupère la liste des produits avec filtres appliqués.
     * Convertit les entités Product en DTOs ProductIndexDto pour la présentation.
     * 
     * Flux :
     * 1. Récupérer les produits filtrés depuis le repository
     * 2. Convertir chaque produit en DTO pour la vue
     * 3. Retourner la liste des DTOs
     * 
     * @param filter les critères de filtrage (nom, prix min/max, catégorie)
     * @return une liste de ProductIndexDto contenant les produits filtrés
     */
    public List<ProductIndexDto> getFilteredProducts(ProductFilter filter) {
        return productRepository.findWithFilter(
                        filter.name(),
                        filter.minPrice(),
                        filter.maxPrice(),
                        filter.categoryId()
                )
                .stream()
                .map(ProductIndexDto::fromEntity)
                .toList();
    }

    /**
     * Récupère tous les détails d'un produit par son ID.
     * Convertit l'entité en DTO ProductDetailsDto.
     * 
     * @param id l'ID du produit à récupérer
     * @return un ProductDetailsDto contenant tous les détails du produit
     * @throws java.util.NoSuchElementException si le produit n'existe pas
     */
    public ProductDetailsDto getProductDetails(Long id) {
        return ProductDetailsDto.fromProduct(
                productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id))
        );
    }

    /**
     * Récupère un produit en entité (non convertie en DTO).
     * Utilisé par le formulaire de modification.
     * 
     * @param id l'ID du produit à récupérer
     * @return l'entité Product
     * @throws java.util.NoSuchElementException si le produit n'existe pas
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Récupère toutes les catégories sous forme de DTOs.
     * Utilisé pour les formulaires de sélection de catégorie.
     * 
     * @return une liste de CategoryDto contenant toutes les catégories
     */
    public List<CategoryDto> getAllCategoriesAsDto() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    /**
     * Récupère toutes les catégories sous forme d'entités.
     * 
     * @return une liste de Category contenant toutes les catégories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Crée un nouveau produit en base de données.
     * Associe automatiquement la catégorie au produit.
     * 
     * Flux :
     * 1. Convertir le formulaire en entité Product
     * 2. Récupérer la catégorie associée
     * 3. Associer la catégorie au produit
     * 4. Sauvegarder le produit en base
     * 
     * @param product l'entité du produit à créer
     * @throws java.util.NoSuchElementException si la catégorie n'existe pas
     */
    public void createProduct(Product product) {
        // Récupérer la catégorie associée au produit
        Category category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategoryId()));

        // Associer la catégorie au produit
        product.setCategory(category);

        // Sauvegarder le produit en base
        productRepository.save(product);
    }

    /**
     * Met à jour un produit existant en base de données.
     * Permet de modifier le nom, description, prix, image et catégorie.
     * 
     * Flux :
     * 1. Récupérer le produit existant
     * 2. Mettre à jour les champs modifiables
     * 3. Si la catégorie a changé, récupérer la nouvelle catégorie et l'associer
     * 4. Sauvegarder les modifications
     * 
     * @param id l'ID du produit à modifier
     * @param product les nouvelles données du produit
     * @throws java.util.NoSuchElementException si le produit ou la catégorie n'existe pas
     */
    public void updateProduct(Long id, Product product) {
        // Récupérer le produit existant en base
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Mettre à jour les champs modifiables
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setImageUrl(product.getImageUrl());

        // Vérifier si la catégorie a changé
        if (!product.getCategoryId().equals(existing.getCategoryId())) {
            Category category = categoryRepository.findById(product.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategoryId()));
            existing.setCategory(category);
        }

        // Sauvegarder les modifications en base
        productRepository.save(existing);
    }

    /**
     * Supprime un produit existant en base de données.
     * 
     * @param id l'ID du produit à supprimer
     * @throws RuntimeException si le produit n'existe pas
     */
    public void deleteProduct(Long id) {
        // Vérifier que le produit existe
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        // Supprimer le produit
        productRepository.deleteById(id);
    }
}
