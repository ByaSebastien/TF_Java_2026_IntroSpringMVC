package be.bstorm.tf_java_2026_introspringmvc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping
    public String home() {
        System.out.println("I m in HomeController");
        return "index";
    }

    @GetMapping("/about")
    public String about(){
        return "about";
    }

    @GetMapping("/say-hello/{name}")
    public String sayHello(
            @PathVariable String name,
            Model model
    ){

        model.addAttribute("name", name);

        return "hello";
    }

    @GetMapping("/say-bye")
    public String sayBye(
            @RequestParam String name,
            Model model
    ){

        model.addAttribute("name", name);

        return "bye";
    }

}
