package be.bstorm.tf_java_2026_introspringmvc.repositories;


import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p where p.name ilike %:name%")
    Optional<Product> findProductByNameContaining(@Param("name") String name);
}
