package be.bstorm.tf_java_2026_introspringmvc.models.cart;

import be.bstorm.tf_java_2026_introspringmvc.entities.CartLine;

import java.util.List;

/**
 * DTO (Data Transfer Object) représentant un panier complet.
 * Utilisé pour transférer les données du panier vers la couche présentation (contrôleur/vue).
 * Contient les lignes du panier ainsi que le montant total.
 * 
 * Avantages du DTO :
 * - Sépare la représentation métier (entité) de celle utilisée dans l'API/la vue
 * - Ajoute des données calculées (totalPrice) sans les persister en base
 * - Facilite le refactoring de l'entité sans casser l'interface publique
 */
public record CartDto(
        /**
         * L'identifiant unique du panier.
         */
        Long cartId,
        
        /**
         * La liste des lignes du panier (produits et quantités).
         * Chaque CartLineDto représente un produit dans le panier.
         */
        List<CartLineDto> cartLines,
        
        /**
         * Le prix total du panier (somme de tous les produits).
         * Calculé automatiquement à partir des lignes du panier.
         */
        Double totalPrice
) {

    /**
     * Crée un CartDto à partir d'une liste d'entités CartLine.
     * Transforme les CartLine en CartLineDto et calcule le montant total.
     * 
     * Flux :
     * 1. Récupérer l'ID du panier depuis la première ligne
     * 2. Convertir chaque CartLine en CartLineDto
     * 3. Calculer la somme des prix (quantité × prix unitaire)
     * 
     * @param cartLines la liste des lignes du panier provenant de la base de données
     * @return un CartDto prêt à être utilisé par la vue
     */
    public static CartDto fromCartLines(List<CartLine> cartLines) {
        return new CartDto(
                cartLines.stream().findFirst().map(cl -> cl.getCart().getId()).orElse(null),
                cartLines.stream().map(CartLineDto::fromCartLine).toList(),
                cartLines.stream().mapToDouble(cl -> cl.getProduct().getPrice() * cl.getQuantity()).sum()
        );
    }
}
