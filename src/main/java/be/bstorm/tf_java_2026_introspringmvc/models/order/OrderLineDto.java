package be.bstorm.tf_java_2026_introspringmvc.models.order;

import be.bstorm.tf_java_2026_introspringmvc.entities.OrderLine;

/**
 * DTO représentant une ligne d'une commande client.
 * 
 * Contient les informations essentielles d'une ligne : le produit, la quantité et le prix au moment de la commande.
 * Utilisé pour afficher les détails d'une commande (vue détail).
 */
public record OrderLineDto(
        /**
         * L'identifiant unique du produit.
         */
        Long productId,

        /**
         * Le nom du produit (snapshot au moment de la commande).
         */
        String productName,

        /**
         * L'image du produit.
         */
        String productImage,

        /**
         * La quantité commandée.
         */
        Integer quantity,

        /**
         * Le prix unitaire du produit au moment de la commande (snapshot).
         * Permet de conserver la traçabilité même si le produit change de prix.
         */
        Double unitPrice,

        /**
         * Le montant total pour cette ligne (quantity × unitPrice).
         */
        Double lineTotal
) {

    /**
     * Crée un OrderLineDto à partir d'une entité OrderLine.
     * 
     * @param orderLine l'entité OrderLine
     * @return un OrderLineDto prêt à être affiché
     */
    public static OrderLineDto fromOrderLine(OrderLine orderLine) {
        return new OrderLineDto(
                orderLine.getProduct().getId(),
                orderLine.getProduct().getName(),
                orderLine.getProduct().getImageUrl(),
                orderLine.getQuantity(),
                orderLine.getPriceSnapshot(),
                orderLine.getLineTotal()
        );
    }
}
