package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.*;
import be.bstorm.tf_java_2026_introspringmvc.enums.StockMovementType;
import be.bstorm.tf_java_2026_introspringmvc.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service métier pour la gestion des stocks et des mouvements.
 * 
 * Responsabilités :
 * - Décrémenter le stock lors d'une commande client
 * - Incrémenter le stock lors d'une réception fournisseur
 * - Créer automatiquement une SupplierOrder si le stock tombe sous le seuil
 * - Enregistrer les mouvements de stock (traçabilité)
 * 
 * Architecture :
 * - Retourne des entités (Stock, StockMovement)
 * - Appelé par OrderService (décrémentation) et Controllers (incrémentation)
 * - N'a PAS de dépendance vers SupplierOrderService pour éviter cycle
 * - S'utilise de manière indépendante, sans orchestration interne
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierOrderLineRepository supplierOrderLineRepository;

    /**
     * Décrémente le stock d'un produit (sortie lors d'une commande expédiée).
     * 
     * Flux :
     * 1. Récupère le stock du produit
     * 2. Décrémente la quantité disponible
     * 3. Crée un mouvement de stock (OUTGOING)
     * 4. Sauvegarde le stock mis à jour
     * 5. Vérifie si le stock tombe sous le seuil → crée une SupplierOrder automatiquement
     * 
     * @param productId l'ID du produit
     * @param quantity la quantité à décrémenter
     * @param order l'entité Order pour la traçabilité (peut être null)
     * @throws IllegalArgumentException si le stock est insuffisant
     */
    @Transactional
    public void decrementStock(Long productId, Integer quantity, Object order) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Stock non trouvé pour le produit : " + productId));

        if (stock.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Stock insuffisant pour le produit " + product.getName() +
                    " : " + stock.getQuantity() + " disponible, " + quantity + " demandé"
            );
        }

        // Décrémenter le stock
        stock.setQuantity(stock.getQuantity() - quantity);
        stock = stockRepository.save(stock);

        // Créer un mouvement de stock (OUTGOING)
        StockMovement movement = new StockMovement(
                StockMovementType.OUTGOING,
                quantity,
                LocalDateTime.now(),
                product
        );
        if (order instanceof be.bstorm.tf_java_2026_introspringmvc.entities.Order) {
            movement.setOrder((be.bstorm.tf_java_2026_introspringmvc.entities.Order) order);
        }
        stockMovementRepository.save(movement);

        // Vérifier si le stock tombe sous le seuil
        if(stock.getQuantity() < stock.getThreshold()) {
            SupplierOrder supplierOrder = new SupplierOrder();
            SupplierOrderLine line = new SupplierOrderLine();
            line.setProduct(product);
            line.setQuantity(stock.getThreshold() * 2); // Quantité suggérée = seuil
            line.setSupplierOrder(supplierOrder);
            supplierOrderLineRepository.save(line);
        }
    }

    /**
     * Incrémente le stock d'un produit (entrée lors d'une réception fournisseur).
     * 
     * Flux :
     * 1. Récupère le stock du produit
     * 2. Incrémente la quantité disponible
     * 3. Crée un mouvement de stock (INCOMING)
     * 4. Sauvegarde le stock mis à jour
     * 
     * @param productId l'ID du produit
     * @param quantity la quantité à incrémenter
     * @param supplierOrder l'entité SupplierOrder pour la traçabilité (peut être null)
     */
    @Transactional
    public void incrementStock(Long productId, Integer quantity, SupplierOrder supplierOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Stock non trouvé pour le produit : " + productId));

        // Incrémenter le stock
        stock.setQuantity(stock.getQuantity() + quantity);
        stock = stockRepository.save(stock);

        // Créer un mouvement de stock (INCOMING)
        StockMovement movement = new StockMovement(
                StockMovementType.INCOMING,
                quantity,
                LocalDateTime.now(),
                product
        );
        if (supplierOrder != null) {
            movement.setSupplierOrder(supplierOrder);
        }
        stockMovementRepository.save(movement);
    }

    /**
     * Vérifie si le stock d'un produit est tombé sous son seuil minimum.
     * 
     * ATTENTION : Cette méthode ne fait que vérifier et log.
     * L'orchestration de la création de SupplierOrder doit être gérée par :
     * - Le Controller (appelant createSupplierOrder() de SupplierOrderService après checkThreshold())
     * - Ou un service orchestrateur dédié
     * 
     * Cette approche évite la dépendance circulaire.
     * 
     * @param productId l'ID du produit à vérifier
     * @return true si le stock est sous le seuil (SupplierOrder devrait être créée), false sinon
     */
    public boolean isUnderThreshold(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Stock non trouvé pour le produit : " + productId));

        return stock.getQuantity() < stock.getThreshold();
    }

    /**
     * Calcule la quantité suggérée pour repassage de commande.
     * Utilisée après checkThreshold() pour créer une SupplierOrder.
     * 
     * @param productId l'ID du produit
     * @return la quantité suggérée (seuil × 2)
     */
    public Integer getSuggestedQuantity(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Stock non trouvé pour le produit : " + productId));

        return stock.getThreshold() * 2;
    }
}
