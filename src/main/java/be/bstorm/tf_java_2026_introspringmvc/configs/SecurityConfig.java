package be.bstorm.tf_java_2026_introspringmvc.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité de l'application.
 * Définit les règles d'accès (authentification, autorisation) et la stratégie de chiffrement.
 * 
 * Annotations :
 * - @Configuration : définit une classe de configuration Spring
 * - @EnableWebSecurity : active Spring Security pour l'application web
 * - @EnableMethodSecurity : active la sécurité au niveau des méthodes (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Crée et configure l'encodeur de mots de passe.
     * BCrypt est un algorithme de hachage sécurisé avec "salt" (clé aléatoire).
     * Jamais stocker les mots de passe en clair ! BCrypt les transforme de manière irréversible.
     * 
     * @return un encodeur BCrypt pour chiffrer les mots de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Crée et configure la chaîne de filtres de sécurité (SecurityFilterChain).
     * Définit :
     * - Quelles URL sont publiques / protégées
     * - Comment l'authentification fonctionne (formulaire login)
     * - Comment la déconnexion fonctionne
     * 
     * @param http l'objet de configuration HTTP
     * @return la chaîne de filtres configurée
     * @throws Exception si une erreur de configuration survient
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF (Cross-Site Request Forgery) : désactivé pour simplifier les tests
                // À réactiver en production !
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configuration des règles d'accès aux URLs
                .authorizeHttpRequests(r -> r
                        // La page /login est accessible uniquement aux anonymes (non connectés)
                        .requestMatchers("/login").anonymous()
                        // La page /logout est accessible uniquement aux utilisateurs authentifiés
                        .requestMatchers("/logout").authenticated()
                        // Toutes les autres URLs sont accessibles sans restriction
                        .anyRequest().permitAll()
                )
                
                // Configuration du formulaire de connexion
                .formLogin(c ->
                        c.loginPage("/login")              // Page du formulaire de login
                                .permitAll()                // Accessible à tous
                                .defaultSuccessUrl("/?logged", true)  // URL après login réussi
                                .failureUrl("/login?error")) // URL après login échoué
                
                // Configuration de la déconnexion
                .logout(c ->
                        c.logoutUrl("/logout")             // URL pour se déconnecter
                                .deleteCookies("JSESSIONID") // Supprimer le cookie de session
                                .invalidateHttpSession(true)  // Invalider la session
                                .logoutSuccessUrl("/login?logout")); // URL après logout
        
        return http.build();

    }
}
