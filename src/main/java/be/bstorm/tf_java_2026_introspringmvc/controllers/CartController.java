package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartLineRepository cartLineRepository;


    @PostMapping("/add/{productId}")
    public String addToCart(
            @PathVariable Long productId
    ){

        Long userId = 1L; // TODO: get the user id from the session or security context
        User user = userRepository.findById(userId).orElseThrow();

        Product product = productRepository.findById(productId).orElseThrow();

        Optional<Cart> cart = cartRepository.findByUserId(userId);

        Cart existingCart;

        if(cart.isEmpty()) {
            Cart newCart = new Cart(user);
            existingCart = cartRepository.save(newCart);
        } else {
            existingCart = cart.get();
        }

        List<CartLine> cartLines = cartLineRepository.findByCartId(existingCart.getId());

        Optional<CartLine> existingLine = cartLines.stream()
                .filter(cl -> cl.getProduct().getId().equals(productId))
                .findFirst();

        if(existingLine.isPresent()) {
            CartLine line = existingLine.get();
            line.setQuantity(line.getQuantity() + 1);
            cartLineRepository.save(line);
        } else {
            CartLine line = new CartLine(1, existingCart, product);
            cartLineRepository.save(line);
        }

        return "redirect:/product";
    }
}
