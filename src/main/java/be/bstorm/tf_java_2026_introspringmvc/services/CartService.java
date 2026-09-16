package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.Cart;
import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartLineRepository cartLineRepository;

    public void addToCart(User user, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();

        Cart cart = computeIfAbsent(user);

        List<CartLine> cartLines = cartLineRepository.findByCartId(cart.getId());

        Optional<CartLine> existingLine = cartLines.stream()
                .filter(cl -> cl.getProduct().getId().equals(productId))
                .findFirst();

        if(existingLine.isPresent()) {
            CartLine line = existingLine.get();
            line.setQuantity(line.getQuantity() + 1);
            cartLineRepository.save(line);
        } else {
            CartLine line = new CartLine(1, cart, product);
            cartLineRepository.save(line);
        }
    }

    private Cart computeIfAbsent(User user) {
        Optional<Cart> cart = cartRepository.findByUserId(user.getId());

        Cart existingCart;

        if(cart.isEmpty()) {
            Cart newCart = new Cart(user);
            existingCart = cartRepository.save(newCart);
        } else {
            existingCart = cart.get();
        }

        return existingCart;
    }
}
