package be.bstorm.tf_java_2026_introspringmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Point d'entrée de l'application Spring Boot.
 * Cette classe lance l'application web et initialise tous les composants Spring.
 * 
 * @SpringBootApplication regroupe :
 * - @Configuration : classe de configuration
 * - @ComponentScan : scanne les composants Spring (@Controller, @Service, etc.)
 * - @EnableAutoConfiguration : active la configuration automatique de Spring Boot
 */
@SpringBootApplication
public class TfJava2026IntroSpringMvcApplication {

    /**
     * Méthode main - point de départ du programme.
     * Lance l'application Spring Boot avec la classe elle-même comme configuration.
     * 
     * @param args arguments de ligne de commande (optionnels)
     */
    public static void main(String[] args) {
        SpringApplication.run(TfJava2026IntroSpringMvcApplication.class, args);
    }

}
