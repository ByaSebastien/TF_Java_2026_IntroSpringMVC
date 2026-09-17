package be.bstorm.tf_java_2026_introspringmvc.models.stock;

import be.bstorm.tf_java_2026_introspringmvc.entities.StockMovement;
import be.bstorm.tf_java_2026_introspringmvc.enums.StockMovementType;

import java.time.LocalDateTime;

/**
 * DTO représentant un mouvement de stock (entrée ou sortie).
 * 
 * Utilisé pour afficher l'historique des mouvements d'un produit.
 * Permet de tracer : "Ce stock a été sorti pour la commande #1001" ou "Reçu de la commande fournisseur #SO-001".
 */
public record StockMovementDto(
        /**
         * L'identifiant unique du mouvement.
         */
        Long movementId,

        /**
         * Le type de mouvement : OUTGOING (sortie) ou INCOMING (entrée).
         */
        StockMovementType type,

        /**
         * L'identifiant du produit affecté.
         */
        Long productId,

        /**
         * Le nom du produit.
         */
        String productName,

        /**
         * La quantité impactée (toujours positive).
         */
        Integer quantity,

        /**
         * La date du mouvement.
         */
        LocalDateTime movementDate,

        /**
         * L'ID de la commande client liée (si type = OUTGOING), null sinon.
         */
        Long orderId,

        /**
         * L'ID de la commande fournisseur liée (si type = INCOMING), null sinon.
         */
        Long supplierOrderId,

        /**
         * Notes ou description du mouvement (ex: "Réception partielle : 8/10 attendus").
         */
        String notes
) {

    /**
     * Crée un StockMovementDto à partir d'une entité StockMovement.
     * 
     * @param movement l'entité StockMovement
     * @return un StockMovementDto prêt à être affiché
     */
    public static StockMovementDto fromStockMovement(StockMovement movement) {
        return new StockMovementDto(
                movement.getId(),
                movement.getType(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getQuantity(),
                movement.getMovementDate(),
                movement.getOrder() != null ? movement.getOrder().getId() : null,
                movement.getSupplierOrder() != null ? movement.getSupplierOrder().getId() : null,
                movement.getNotes()
        );
    }
}
