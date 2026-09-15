package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartLineRepository extends JpaRepository<CartLine, CartLine.CartLineId> {

    @Query("select cl from CartLine cl join cl.cart c join cl.product where c.id = :cartId")
    List<CartLine> findByCartId (Long cartId);
}
