package be.bstorm.tf_java_2026_introspringmvc.repositories;

import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'accès aux données des utilisateurs en base de données.
 * Spring Data JPA génère automatiquement l'implémentation pour les opérations CRUD standards.
 * Ce repository ajoute des méthodes personnalisées pour des recherches spécifiques.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Cherche un utilisateur par son nom d'utilisateur.
     * Utilisé notamment lors de l'authentification pour charger les détails de l'utilisateur.
     * @param username le nom d'utilisateur à chercher
     * @return un Optional contenant l'utilisateur s'il existe, vide sinon
     */
    Optional<User> findUserByUsername(String username);
}
