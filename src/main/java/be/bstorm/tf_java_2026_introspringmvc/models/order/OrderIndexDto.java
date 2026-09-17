package be.bstorm.tf_java_2026_introspringmvc.models.order;

import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour afficher une commande client dans une liste.
 * Contient les informations essentielles sans les détails des lignes.
 * 
 * Utilisé pour la vue du magasinier (liste des commandes à traiter).
 */
public record OrderIndexDto(
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
         * Le nombre total d'articles (lignes) dans la commande.
         */
        Integer lineCount,

        /**
         * Le statut actuel de la commande (PENDING, SHIPPED, CANCELLED).
         */
        OrderStatus status
) {

    /**
     * Crée un OrderIndexDto à partir d'une entité Order.
     * Note : le lineCount doit être fourni par la couche service/controller
     * via une requête (car pas de OneToMany bidirectionnel).
     * 
     * @param order l'entité Order
     * @param lineCount le nombre de lignes dans la commande
     * @return un OrderIndexDto prêt à être affiché
     */
    public static OrderIndexDto fromOrder(Order order, Integer lineCount) {
        return new OrderIndexDto(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getCreatedAt(),
                order.getTotalAmount(),
                lineCount,
                order.getStatus()
        );
    }
}
