package be.bstorm.tf_java_2026_introspringmvc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour les pages publiques (accueil, À propos).
 * Un contrôleur reçoit les requêtes HTTP et retourne des réponses (HTML, JSON, redirections).
 * 
 * Spring MVC utilise le pattern MVC (Model-View-Controller) :
 * - Request HTTP arrive → Contrôleur (traitement logique) → Model (données) → View (template HTML)
 */
@Controller
public class HomeController {

    /**
     * Affiche la page d'accueil du site.
     * @return le nom du template à afficher : "index"
     */
    @GetMapping
    public String home() {
        System.out.println("I m in HomeController");
        return "index";
    }

    /**
     * Affiche la page "À propos".
     * @return le nom du template à afficher : "about"
     */
    @GetMapping("/about")
    public String about(){
        return "about";
    }

    /**
     * Affiche un message de salutation personnalisé.
     * Utilise une variable de chemin (PathVariable) pour récupérer le nom de l'URL.
     * 
     * Exemple : /say-hello/Jean retourne un page avec "Bonjour Jean"
     * 
     * @param name le nom extrait de l'URL (ex: /say-hello/{name})
     * @param model l'objet pour passer des données à la vue
     * @return le nom du template à afficher : "hello"
     */
    @GetMapping("/say-hello/{name}")
    public String sayHello(
            @PathVariable String name,
            Model model
    ){

        model.addAttribute("name", name);

        return "hello";
    }

    /**
     * Affiche un message d'au revoir personnalisé.
     * Utilise un RequestParam pour récupérer un paramètre de la query string.
     * 
     * Exemple : /say-bye?name=Jean affiche "Au revoir Jean"
     * 
     * @param name le nom fourni en paramètre de requête (?name=)
     * @param model l'objet pour passer des données à la vue
     * @return le nom du template à afficher : "bye"
     */
    @GetMapping("/say-bye")
    public String sayBye(
            @RequestParam String name,
            Model model
    ){

        model.addAttribute("name", name);

        return "bye";
    }

}
