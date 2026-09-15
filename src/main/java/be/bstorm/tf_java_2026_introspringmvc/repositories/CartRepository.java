package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("select c from Cart c join c.user u where u.id = :userId")
    Optional<Cart> findByUserId(Long userId);
}
