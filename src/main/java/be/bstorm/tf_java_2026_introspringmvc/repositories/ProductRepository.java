package be.bstorm.tf_java_2026_introspringmvc.repositories;


import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des produits en base de données.
 * Fournit les opérations CRUD standard ainsi que des méthodes de recherche avancée.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Recherche les produits avec des filtres optionnels.
     * Les filtres null sont ignorés (ne contraignent pas la recherche).
     * 
     * Exemples :
     * - findWithFilter(null, 10.0, 100.0, null) = tous les produits entre 10€ et 100€
     * - findWithFilter("Gants", null, null, 1) = produits contenant "Gants" dans la catégorie 1
     *
     * @param name le nom ou partie du nom (null pour ignorer)
     * @param minPrice le prix minimum (null pour ignorer)
     * @param maxPrice le prix maximum (null pour ignorer)
     * @param categoryId l'ID de la catégorie (null pour ignorer)
     * @return une liste de produits correspondant aux critères
     */
    @Query("select p " +
            "from Product p " +
            "where (:name is null or p.name ilike %:name%) and " +
            "(:minPrice is null or p.price >= :minPrice) and " +
            "(:maxPrice is null or p.price <= :maxPrice) and " +
            "(:categoryId is null or p.categoryId = :categoryId)")
    List<Product> findWithFilter(String name, Double minPrice, Double maxPrice, Long categoryId);
}
