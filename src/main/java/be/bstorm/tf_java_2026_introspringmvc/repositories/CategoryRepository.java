package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
