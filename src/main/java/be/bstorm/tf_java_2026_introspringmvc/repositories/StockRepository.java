package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'accès aux données des stocks en base de données.
 * 
 * Responsabilités :
 * - Récupérer le stock d'un produit
 * - Mettre à jour les quantités disponibles
 * - Requêtes pour les produits sous le seuil (alerte)
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Cherche le stock associé à un produit spécifique.
     * Utilise une requête inverse : cherche le Stock dont l'ID est celui du stock du Product.
     * 
     * @param product l'entité Product
     * @return un Optional contenant le Stock si trouvé
     */
    @Query("select s from Stock s where s.id = (select p.stock.id from Product p where p = :product)")
    Optional<Stock> findByProduct(Product product);

    /**
     * Cherche le stock d'un produit par son ID.
     * 
     * @param productId l'ID du produit
     * @return un Optional contenant le Stock si trouvé
     */
    @Query("select p.stock from Product p where p.id = :productId")
    Optional<Stock> findByProductId(Long productId);
}
