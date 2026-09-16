package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification qui charge les détails des utilisateurs depuis la base de données.
 * Implémente UserDetailsService de Spring Security, qui est responsable de fournir
 * les informations utilisateur lors du processus de connexion.
 * 
 * Flux typique :
 * 1. L'utilisateur soumet ses identifiants (username/password) via le formulaire de login
 * 2. Spring Security appelle loadUserByUsername() avec le username
 * 3. Cette méthode retourne l'entité User qui implémente UserDetails
 * 4. Spring Security compare le mot de passe soumis avec celui en base (chiffré en BCrypt)
 */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    /**
     * Repository pour accéder aux utilisateurs en base de données.
     * Injecté automatiquement par Spring (Dependency Injection).
     */
    private final UserRepository userRepository;

    /**
     * Charge les détails d'un utilisateur par son nom d'utilisateur.
     * Méthode requise par l'interface UserDetailsService de Spring Security.
     * 
     * @param username le nom d'utilisateur à chercher
     * @return les détails de l'utilisateur (qui implémente UserDetails)
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond à ce username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User with username " + username + " not found")
        );
    }
}
