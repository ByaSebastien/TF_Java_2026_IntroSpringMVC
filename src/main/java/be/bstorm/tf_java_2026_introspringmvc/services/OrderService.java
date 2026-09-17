package be.bstorm.tf_java_2026_introspringmvc.services;

import be.bstorm.tf_java_2026_introspringmvc.entities.*;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.CartRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service métier pour la gestion des commandes client.
 * 
 * Responsabilités :
 * - Créer une commande à partir du panier (passage Cart → Order)
 * - Valider une commande (magasinier) : passage de PENDING à SHIPPED
 * - Annuler une commande
 * - Récupérer les commandes par statut ou utilisateur
 * 
 * Architecture :
 * - Aucun mapping ici (le Controller s'en charge)
 * - Retourne des entités Order (utilisées par Controller pour mapping DTO)
 * - Orchestration des repositories pour la logique métier
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final CartRepository cartRepository;
    private final CartLineRepository cartLineRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    /**
     * Crée une nouvelle commande à partir du panier d'un utilisateur.
     * 
     * Flux :
     * 1. Récupère le panier de l'utilisateur
     * 2. Récupère les lignes du panier via CartLineRepository (pas de mappedBy, pas de getLines())
     * 3. Crée une entité Order (statut PENDING)
     * 4. Crée les OrderLines à partir des CartLines
     * 5. Calcule le montant total
     * 6. Sauvegarde la commande et ses lignes
     * 7. Vide les CartLines
     * 
     * Cette opération est transactionnelle : si une erreur survient,
     * le panier n'est pas vidé et la commande n'est pas créée.
     * 
     * @param userId l'ID de l'utilisateur qui crée la commande
     * @return l'entité Order créée (avec ID généré)
     * @throws IllegalArgumentException si l'utilisateur n'existe pas ou le panier est vide
     */
    @Transactional
    public Order createOrderFromCart(Long userId) {
        // Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé : " + userId));

        // Récupérer le panier
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Panier vide ou inexistant pour l'utilisateur : " + userId));

        // Récupérer les lignes du panier via CartLineRepository (pas de mappedBy, pas de getLines())
        List<CartLine> cartLines = cartLineRepository.findByCartId(cart.getId());

        if (cartLines == null || cartLines.isEmpty()) {
            throw new IllegalArgumentException("Panier vide : impossible de créer une commande");
        }

        // Créer la commande
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        // Sauvegarder la commande d'abord (pour obtenir l'ID)
        order = orderRepository.save(order);

        // Créer les OrderLines à partir des CartLines
        double totalAmount = 0;
        for (CartLine cartLine : cartLines) {
            OrderLine orderLine = new OrderLine(
                    cartLine.getQuantity(),
                    cartLine.getProduct().getPrice(),  // snapshot du prix
                    order,
                    cartLine.getProduct()
            );
            orderLineRepository.save(orderLine);
            totalAmount += orderLine.getLineTotal();
        }

        // Mettre à jour le montant total de la commande
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Vider les CartLines via CartLineRepository
        // Note: @Modifying nécessite flush() pour s'exécuter immédiatement
        orderRepository.flush();
        cartLineRepository.deleteByCartId(cart.getId());

        return order;
    }

    /**
     * Valide une commande avec gestion du cas partiellement disponible (split).
     * 
     * Flux :
     * 1. Si toutes les quantités demandées sont disponibles :
     *    - Order.status = SHIPPED
     *    - Décrémente stocks pour tous les produits
     *    - Crée auto SupplierOrders si stock < seuil
     * 
     * 2. Si partiellement disponible (au moins 1 produit manquant) :
     *    - Order initiale → CANCELLED
     *    - Order 1 (expédié) → SHIPPED avec lignes disponibles
     *    - Order 2 (manquant) → PENDING avec lignes partielles/manquantes
     *    - Décrémente stocks UNIQUEMENT pour Order 1
     *    - Crée auto SupplierOrders si stock < seuil (après décrémentation)
     * 
     * Utilisé par le magasinier pour valider et expédier une commande.
     * 
     * @param orderId l'ID de la commande à valider
     * @param availableQuantities Map<ProductId, QuantityAvailable> (quantités disponibles par produit)
     * @throws IllegalArgumentException si la commande n'existe pas ou n'est pas en PENDING
     */
    @Transactional
    public void validateOrder(Long orderId, Map<Long, Integer> availableQuantities) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Seules les commandes PENDING peuvent être validées");
        }

        List<OrderLine> lines = orderLineRepository.findByOrderId(orderId);
        
        // Vérifier si toutes les quantités demandées sont disponibles
        boolean allAvailable = lines.stream().allMatch(line ->
                availableQuantities.getOrDefault(line.getProduct().getId(), 0) >= line.getQuantity()
        );

        if (allAvailable) {
            // Cas 1: Tout est disponible → Expédier directement
            for (OrderLine line : lines) {
                stockService.decrementStock(line.getProduct().getId(), line.getQuantity(), order);
            }
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        } else {
            // Cas 2: Partiellement disponible → Split en 2 commandes
            Order shippedOrder = new Order();
            shippedOrder.setUser(order.getUser());
            shippedOrder.setStatus(OrderStatus.SHIPPED);
            shippedOrder = orderRepository.save(shippedOrder);

            Order pendingOrder = new Order();
            pendingOrder.setUser(order.getUser());
            pendingOrder.setStatus(OrderStatus.PENDING);
            pendingOrder = orderRepository.save(pendingOrder);

            double shippedTotal = 0;
            double pendingTotal = 0;

            for (OrderLine line : lines) {
                int available = availableQuantities.getOrDefault(line.getProduct().getId(), 0);
                int demanded = line.getQuantity();

                if (available > 0) {
                    // Créer OrderLine pour shippedOrder
                    OrderLine shippedLine = new OrderLine();
                    shippedLine.setOrder(shippedOrder);
                    shippedLine.setProduct(line.getProduct());
                    shippedLine.setQuantity(available);
                    shippedLine.setPriceSnapshot(line.getPriceSnapshot());
                    orderLineRepository.save(shippedLine);
                    shippedTotal += available * line.getPriceSnapshot();

                    // Décrémenter stock
                    stockService.decrementStock(line.getProduct().getId(), available, shippedOrder);
                }

                if (available < demanded) {
                    // Créer OrderLine pour pendingOrder
                    OrderLine pendingLine = new OrderLine();
                    pendingLine.setOrder(pendingOrder);
                    pendingLine.setProduct(line.getProduct());
                    pendingLine.setQuantity(demanded - available);
                    pendingLine.setPriceSnapshot(line.getPriceSnapshot());
                    orderLineRepository.save(pendingLine);
                    pendingTotal += (demanded - available) * line.getPriceSnapshot();
                }
            }

            shippedOrder.setTotalAmount(shippedTotal);
            pendingOrder.setTotalAmount(pendingTotal);
            orderRepository.save(shippedOrder);
            orderRepository.save(pendingOrder);

            // Annuler la commande initiale
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }

    /**
     * Annule une commande et passe au statut CANCELLED.
     * 
     * Utilisé si une commande doit être annulée (raison : indisponibilité partielle, etc).
     * 
     * @param orderId l'ID de la commande à annuler
     * @return l'entité Order mise à jour (statut = CANCELLED)
     * @throws IllegalArgumentException si la commande n'existe pas
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Récupère toutes les commandes d'un utilisateur.
     * 
     * @param userId l'ID de l'utilisateur
     * @return une liste de commandes de cet utilisateur
     */
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Récupère toutes les commandes en statut PENDING.
     * Utilisé par le magasinier pour voir ses tâches.
     * 
     * @return une liste de commandes à traiter
     */
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }

    /**
     * Récupère une commande par son ID.
     * 
     * @param orderId l'ID de la commande
     * @return l'entité Order
     * @throws IllegalArgumentException si la commande n'existe pas
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));
    }
}
