package be.bstorm.tf_java_2026_introspringmvc.models.order;

import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour afficher les détails complets d'une commande client.
 * 
 * Contient la commande avec toutes ses lignes (OrderLine).
 * Utilisé pour la vue détail d'une commande (client ou magasinier).
 */
public record OrderDetailsDto(
        /**
         * L'identifiant unique de la commande.
         */
        Long orderId,

        /**
         * L'ID de l'utilisateur qui a passé la commande.
         */
        Long userId,

        /**
         * Le nom de l'utilisateur qui a passé la commande.
         */
        String userName,

        /**
         * La date de création de la commande.
         */
        LocalDateTime createdAt,

        /**
         * Le montant total de la commande.
         */
        Double totalAmount,

        /**
         * Le statut actuel de la commande (PENDING, SHIPPED, CANCELLED).
         */
        OrderStatus status,

        /**
         * Liste complète des lignes de cette commande (les produits commandés).
         */
        List<OrderLineDto> lines
) {

    /**
     * Crée un OrderDetailsDto à partir d'une entité Order et ses lignes.
     * 
     * @param order l'entité Order
     * @param lines la liste des OrderLineDto
     * @return un OrderDetailsDto prêt à être affiché
     */
    public static OrderDetailsDto fromOrder(Order order, List<OrderLineDto> lines) {
        return new OrderDetailsDto(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getCreatedAt(),
                order.getTotalAmount(),
                order.getStatus(),
                lines
        );
    }
}
