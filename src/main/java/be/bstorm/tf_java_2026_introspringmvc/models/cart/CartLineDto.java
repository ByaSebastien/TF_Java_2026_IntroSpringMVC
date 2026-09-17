package be.bstorm.tf_java_2026_introspringmvc.models.cart;

import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;
import be.bstorm.tf_java_2026_introspringmvc.models.product.ProductIndexDto;

/**
 * DTO (Data Transfer Object) représentant une ligne du panier.
 * Contient les informations d'un produit dans le panier (produit, quantité et prix total de la ligne).
 * 
 * Utilisé uniquement pour la présentation et ne doit pas être persisté directement en base.
 * Les données sont calculées à partir de l'entité CartLine.
 */
public record CartLineDto(
        /**
         * Les informations du produit dans cette ligne du panier.
         */
        ProductIndexDto product,
        
        /**
         * La quantité du produit souhaité par l'utilisateur.
         * Doit être > 0 pour qu'une ligne soit valide.
         */
        Integer quantity,
        
        /**
         * Le prix total de cette ligne (quantité × prix unitaire du produit).
         * Calculé automatiquement, non saisi par l'utilisateur.
         */
        Double totalPrice
) {

    /**
     * Crée un CartLineDto à partir d'une entité CartLine.
     * Extrait les informations du produit et calcule le prix total.
     * 
     * Flux :
     * 1. Récupérer les informations du produit et les convertir en ProductIndexDto
     * 2. Récupérer la quantité de la CartLine
     * 3. Calculer le prix total : quantité × prix unitaire du produit
     * 
     * @param cartLine l'entité CartLine provenant de la base de données
     * @return un CartLineDto prêt à être utilisé par la vue
     */
    public static CartLineDto fromCartLine(CartLine cartLine) {

        return new CartLineDto(
                ProductIndexDto.fromEntity(cartLine.getProduct()),
                cartLine.getQuantity(),
                cartLine.getProduct().getPrice() * cartLine.getQuantity()
        );
    }
}
