package be.bstorm.tf_java_2026_introspringmvc.controllers;


import be.bstorm.tf_java_2026_introspringmvc.models.user.LoginForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String login(
            Model model
    ) {

        model.addAttribute("form", new LoginForm());

        return "/auth/login";
    }
}
