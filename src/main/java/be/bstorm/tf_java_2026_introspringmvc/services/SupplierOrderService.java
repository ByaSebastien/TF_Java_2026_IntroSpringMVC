package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.Product;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service métier pour la gestion des commandes fournisseur.
 * 
 * Responsabilités :
 * - Créer une SupplierOrder automatiquement quand un stock tombe sous le seuil
 * - Permettre au chef de rayon d'éditer les quantités (tant qu'en DRAFT)
 * - Soumettre une commande (DRAFT → ORDERED, immuable)
 * - Recevoir une commande (ORDERED → RECEIVED)
 * - Gérer les requêtes de filtrage par statut
 * 
 * Architecture :
 * - Retourne des entités SupplierOrder
 * - Appelé par StockService (création automatique) et Controllers (gestion manuelle)
 * - N'appelle PAS StockService pour éviter dépendance circulaire
 * - Le Controller orchestre : receiveSupplierOrder() + incrementStock() dans une même @Transactional
 */
@Service
@RequiredArgsConstructor
public class SupplierOrderService {

    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierOrderLineRepository supplierOrderLineRepository;
    private final ProductRepository productRepository;

    /**
     * Crée une nouvelle commande fournisseur automatiquement.
     * Utilisée quand un stock tombe sous le seuil minimum.
     * 
     * Flux :
     * 1. Crée une SupplierOrder en statut DRAFT
     * 2. Crée une SupplierOrderLine avec le produit et la quantité suggérée
     * 3. Sauvegarde le tout en base
     * 
     * @param productId l'ID du produit à commander
     * @param suggestedQuantity la quantité suggérée (basée sur threshold × 2)
     * @return l'entité SupplierOrder créée
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public SupplierOrder createSupplierOrder(Long productId, Integer suggestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        // Créer la commande fournisseur
        SupplierOrder supplierOrder = new SupplierOrder();
        supplierOrder.setStatus(SupplierOrderStatus.DRAFT);
        supplierOrder.setCreatedAt(LocalDateTime.now());
        supplierOrder = supplierOrderRepository.save(supplierOrder);

        // Créer la ligne avec le produit et la quantité suggérée
        SupplierOrderLine line = new SupplierOrderLine(
                suggestedQuantity,
                supplierOrder,
                product
        );
        supplierOrderLineRepository.save(line);

        return supplierOrder;
    }

    /**
     * Soumet une commande fournisseur (passage DRAFT → ORDERED).
     * Une fois ORDERED, la commande devient immuable (le chef de rayon ne peut plus éditer les quantités).
     * 
     * Utilisé par le chef de rayon pour valider et envoyer la commande au fournisseur.
     * 
     * @param supplierOrderId l'ID de la commande à soumettre
     * @return l'entité SupplierOrder mise à jour (statut = ORDERED)
     * @throws IllegalArgumentException si la commande n'existe pas ou n'est pas en DRAFT
     */
    @Transactional
    public SupplierOrder submitSupplierOrder(Long supplierOrderId) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.canBeSubmitted()) {
            throw new IllegalArgumentException("Seules les commandes en DRAFT peuvent être soumises");
        }

        supplierOrder.setStatus(SupplierOrderStatus.ORDERED);
        return supplierOrderRepository.save(supplierOrder);
    }

    /**
     * Reçoit une commande fournisseur avec gestion du cas partiellement reçu (split).
     * 
     * Flux :
     * 1. Si toutes les quantités commandées sont reçues :
     *    - SupplierOrder.status = RECEIVED
     *    - Incrémente stocks pour tous les produits
     * 
     * 2. Si partiellement reçue (au moins 1 produit manquant) :
     *    - SupplierOrder initiale → CANCELLED
     *    - SupplierOrder 1 (reçue) → RECEIVED avec lignes reçues
     *    - SupplierOrder 2 (manquante) → ORDERED avec lignes manquantes
     *    - Incrémente stocks UNIQUEMENT pour SupplierOrder 1
     * 
     * Utilisé par le magasinier pour valider la réception.
     * 
     * @param supplierOrderId l'ID de la commande à recevoir
     * @param receivedQuantities Map<ProductId, QuantityReceived> (quantités reçues par produit)
     * @throws IllegalArgumentException si la commande n'existe pas ou n'est pas en ORDERED
     */
    @Transactional
    public void receiveSupplierOrder(Long supplierOrderId, Map<Long, Integer> receivedQuantities) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.canBeReceived()) {
            throw new IllegalArgumentException("Seules les commandes en ORDERED peuvent être reçues");
        }

        List<SupplierOrderLine> lines = supplierOrderLineRepository.findBySupplierOrderId(supplierOrderId);

        // Vérifier si toutes les quantités commandées sont reçues
        boolean allReceived = lines.stream().allMatch(line ->
                receivedQuantities.getOrDefault(line.getProduct().getId(), 0) >= line.getQuantity()
        );

        if (allReceived) {
            // Cas 1: Tout est reçu → Marquer comme RECEIVED
            supplierOrder.setStatus(SupplierOrderStatus.RECEIVED);
            supplierOrderRepository.save(supplierOrder);
        } else {
            // Cas 2: Partiellement reçu → Split en 2 commandes
            SupplierOrder receivedOrder = new SupplierOrder();
            receivedOrder.setStatus(SupplierOrderStatus.RECEIVED);
            receivedOrder.setCreatedAt(LocalDateTime.now());
            receivedOrder = supplierOrderRepository.save(receivedOrder);

            SupplierOrder pendingOrder = new SupplierOrder();
            pendingOrder.setStatus(SupplierOrderStatus.ORDERED);
            pendingOrder.setCreatedAt(LocalDateTime.now());
            pendingOrder = supplierOrderRepository.save(pendingOrder);

            for (SupplierOrderLine line : lines) {
                int received = receivedQuantities.getOrDefault(line.getProduct().getId(), 0);
                int ordered = line.getQuantity();

                if (received > 0) {
                    // Créer SupplierOrderLine pour receivedOrder
                    SupplierOrderLine receivedLine = new SupplierOrderLine(
                            received,
                            receivedOrder,
                            line.getProduct()
                    );
                    supplierOrderLineRepository.save(receivedLine);
                }

                if (received < ordered) {
                    // Créer SupplierOrderLine pour pendingOrder
                    SupplierOrderLine pendingLine = new SupplierOrderLine(
                            ordered - received,
                            pendingOrder,
                            line.getProduct()
                    );
                    supplierOrderLineRepository.save(pendingLine);
                }
            }

            // Annuler la commande initiale
            supplierOrder.setStatus(SupplierOrderStatus.CANCELLED);
            supplierOrderRepository.save(supplierOrder);
        }
    }

    /**
     * Annule une commande fournisseur.
     * Utile si la commande doit être annulée avant sa soumission ou réception.
     * 
     * @param supplierOrderId l'ID de la commande à annuler
     * @return l'entité SupplierOrder mise à jour (statut = CANCELLED)
     * @throws IllegalArgumentException si la commande n'existe pas
     */
    @Transactional
    public SupplierOrder cancelSupplierOrder(Long supplierOrderId) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        supplierOrder.setStatus(SupplierOrderStatus.CANCELLED);
        return supplierOrderRepository.save(supplierOrder);
    }

    /**
     * Récupère toutes les commandes fournisseur en statut DRAFT.
     * Utilisé par le chef de rayon pour éditer les commandes.
     * 
     * @return une liste de commandes en brouillon
     */
    public List<SupplierOrder> getDraftOrders() {
        return supplierOrderRepository.findDraftOrders();
    }

    /**
     * Récupère toutes les commandes fournisseur en statut ORDERED.
     * Utilisé par le magasinier pour valider les réceptions.
     * 
     * @return une liste de commandes à recevoir
     */
    public List<SupplierOrder> getOrderedOrders() {
        return supplierOrderRepository.findOrderedOrders();
    }

    /**
     * Récupère toutes les commandes fournisseur en statut RECEIVED.
     * Utilisé pour l'historique et le reporting.
     * 
     * @return une liste de commandes reçues
     */
    public List<SupplierOrder> getReceivedOrders() {
        return supplierOrderRepository.findReceivedOrders();
    }

    /**
     * Récupère une commande fournisseur par son ID.
     * 
     * @param supplierOrderId l'ID de la commande
     * @return l'entité SupplierOrder
     * @throws IllegalArgumentException si la commande n'existe pas
     */
    public SupplierOrder getSupplierOrderById(Long supplierOrderId) {
        return supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));
    }

    /**
     * Récupère toutes les lignes d'une commande fournisseur.
     * 
     * @param supplierOrderId l'ID de la commande
     * @return une liste de SupplierOrderLine
     */
    public List<SupplierOrderLine> getSupplierOrderLines(Long supplierOrderId) {
        return supplierOrderLineRepository.findBySupplierOrderId(supplierOrderId);
    }

    /**
     * Modifie la quantité d'une ligne de commande fournisseur.
     * Possible uniquement si la commande est en statut DRAFT (sinon elle est immuable).
     * 
     * @param supplierOrderId l'ID de la commande
     * @param productId l'ID du produit à modifier
     * @param newQuantity la nouvelle quantité
     * @throws IllegalArgumentException si la commande n'existe pas, n'est pas DRAFT, ou la ligne n'existe pas
     */
    @Transactional
    public void updateLineQuantity(Long supplierOrderId, Long productId, Integer newQuantity) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.isEditable()) {
            throw new IllegalArgumentException("Seules les commandes en DRAFT peuvent être éditées");
        }

        SupplierOrderLine line = supplierOrderLineRepository.findBySupplierOrderAndProduct(supplierOrderId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne non trouvée pour le produit : " + productId));

        line.setQuantity(newQuantity);
        supplierOrderLineRepository.save(line);
    }

    /**
     * Supprime une ligne de commande fournisseur.
     * Possible uniquement si la commande est en statut DRAFT.
     * 
     * @param supplierOrderId l'ID de la commande
     * @param productId l'ID du produit à supprimer
     * @throws IllegalArgumentException si la commande n'existe pas, n'est pas DRAFT, ou la ligne n'existe pas
     */
    @Transactional
    public void deleteLineByProduct(Long supplierOrderId, Long productId) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.isEditable()) {
            throw new IllegalArgumentException("Seules les commandes en DRAFT peuvent être éditées");
        }

        supplierOrderLineRepository.deleteBySupplierOrderAndProduct(supplierOrderId, productId);
    }

    /**
     * Ajoute une nouvelle ligne à une commande fournisseur.
     * Possible uniquement si la commande est en statut DRAFT.
     * 
     * @param supplierOrderId l'ID de la commande
     * @param productId l'ID du produit à ajouter
     * @param quantity la quantité à commander
     * @throws IllegalArgumentException si la commande n'existe pas, n'est pas DRAFT, ou le produit n'existe pas
     */
    @Transactional
    public void addLine(Long supplierOrderId, Long productId, Integer quantity) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.isEditable()) {
            throw new IllegalArgumentException("Seules les commandes en DRAFT peuvent être éditées");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé : " + productId));

        SupplierOrderLine line = new SupplierOrderLine(
                quantity,
                supplierOrder,
                product
        );
        supplierOrderLineRepository.save(line);
    }

    /**
     * Supprime une commande fournisseur entière (DRAFT uniquement).
     * Supprime aussi toutes les lignes associées.
     * 
     * @param supplierOrderId l'ID de la commande
     * @throws IllegalArgumentException si la commande n'existe pas ou n'est pas DRAFT
     */
    @Transactional
    public void deleteSupplierOrder(Long supplierOrderId) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

        if (!supplierOrder.getStatus().equals(SupplierOrderStatus.DRAFT)) {
            throw new IllegalArgumentException("Seules les commandes en DRAFT peuvent être supprimées (statut : " + supplierOrder.getStatus() + ")");
        }

        // Supprimer les lignes
        supplierOrderLineRepository.deleteBySupplierOrderId(supplierOrderId);

        // Supprimer la commande
        supplierOrderRepository.deleteById(supplierOrderId);
    }
}
