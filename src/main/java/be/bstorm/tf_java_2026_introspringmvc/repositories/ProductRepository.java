package be.bstorm.tf_java_2026_introspringmvc.repositories;


import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p " +
            "from Product p " +
            "where (:name is null or p.name ilike %:name%) and " +
            "(:minPrice is null or p.price >= :minPrice) and " +
            "(:maxPrice is null or p.price <= :maxPrice) and " +
            "(:categoryId is null or p.categoryId = :categoryId)")
    List<Product> findWithFilter(String name, Double minPrice, Double maxPrice, Long categoryId);
}
