package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.models.order.OrderDetailsDto;
import be.bstorm.tf_java_2026_introspringmvc.models.order.OrderLineDto;
import be.bstorm.tf_java_2026_introspringmvc.models.supplierorder.SupplierOrderDto;
import be.bstorm.tf_java_2026_introspringmvc.models.supplierorder.SupplierOrderLineDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.services.OrderService;
import be.bstorm.tf_java_2026_introspringmvc.services.StockService;
import be.bstorm.tf_java_2026_introspringmvc.services.SupplierOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour les opérations de magasinage (rôle ROLE_WAREHOUSEMAN).
 * 
 * Responsabilités :
 * - Afficher les commandes client en attente de validation (PENDING)
 * - Valider/expédier une commande (PENDING → SHIPPED) = décrémente les stocks
 * - Afficher les commandes fournisseur en attente de réception (ORDERED)
 * - Recevoir une commande fournisseur (ORDERED → RECEIVED) = incrémente les stocks
 * 
 * Orchestration CRITIQUE :
 * - receiveSupplierOrder() + incrementStock() sont dans une même @Transactional
 * - Ceci évite la dépendance circulaire (SupplierOrderService N'appelle PAS StockService)
 * 
 * Routes disponibles : /warehouse/...
 * Accès restreint : ROLE_WAREHOUSEMAN uniquement
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse")
public class WarehouseController {

    private final OrderService orderService;
    private final SupplierOrderService supplierOrderService;
    private final StockService stockService;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierOrderLineRepository supplierOrderLineRepository;

    /**
     * Affiche la liste des commandes client en attente de validation (PENDING).
     * 
     * Flux :
     * 1. Récupère toutes les commandes avec statut PENDING via OrderService
     * 2. Mappe chaque Order en OrderDetailsDto pour l'affichage
     * 3. Ajoute la liste au modèle
     * 4. Retourne le template pour afficher la liste
     * 
     * @param model le modèle pour passer les données à la vue
     * @return le template warehouse/pending-orders.html
     */
    @GetMapping("/orders/pending")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    public String getPendingOrders(Model model) {
        List<Order> pendingOrders = orderService.getPendingOrders();
        
        List<OrderDetailsDto> orderDtos = pendingOrders.stream()
                .map(this::mapOrderToDetailsDto)
                .collect(Collectors.toList());
        
        model.addAttribute("orders", orderDtos);
        return "warehouse/pending-orders";
    }

    /**
     * Affiche les détails d'une commande client spécifique.
     * 
     * Flux :
     * 1. Récupère la commande par ID
     * 2. Mappe en OrderDetailsDto avec ses lignes
     * 3. Ajoute au modèle
     * 
     * @param orderId l'ID de la commande
     * @param model le modèle
     * @return le template warehouse/order-detail.html ou redirect si non trouvée
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));
        
        OrderDetailsDto orderDto = mapOrderToDetailsDto(order);
        model.addAttribute("order", orderDto);
        return "warehouse/order-detail";
    }

    /**
     * Valide une commande client (PENDING → SHIPPED ou split si partiellement disponible).
     * 
     * Flux :
     * 1. Récupère les quantités disponibles du formulaire (Map)
     * 2. Appelle OrderService.validateOrder() qui implémente le split
     * 3. Si tout disponible : PENDING → SHIPPED, stocks décrmentés
     * 4. Si partiellement disponible : split en 2 commandes (SHIPPED + PENDING)
     * 
     * @param orderId l'ID de la commande
     * @param quantities Map<productId, quantityAvailable> depuis le formulaire
     * @param attributes pour passer un message de succès
     * @return redirect vers /warehouse/orders/pending
     */
    @PostMapping("/orders/{orderId}/validate")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    @Transactional
    public String validateOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) Map<String, String> quantities,
            RedirectAttributes attributes
    ) {
        try {
            // Convertir les quantités du formulaire en Map<Long, Integer>
            Map<Long, Integer> availableQuantities = new HashMap<>();
            if (quantities != null) {
                quantities.forEach((key, value) -> {
                    if (key.startsWith("quantities[") && key.endsWith("]")) {
                        String productIdStr = key.substring("quantities[".length(), key.length() - 1);
                        try {
                            Long productId = Long.parseLong(productIdStr);
                            Integer qty = Integer.parseInt(value);
                            availableQuantities.put(productId, qty);
                        } catch (NumberFormatException e) {
                            // Ignorer les conversions invalides
                        }
                    }
                });
            }

            orderService.validateOrder(orderId, availableQuantities);
            attributes.addFlashAttribute("success", "Commande validée");
            return "redirect:/warehouse/orders/pending";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/warehouse/orders/" + orderId;
        }
    }

    /**
     * Affiche la liste des commandes fournisseur en attente de réception (ORDERED).
     * 
     * Flux :
     * 1. Récupère les SupplierOrder avec statut ORDERED
     * 2. Mappe en SupplierOrderDto pour l'affichage
     * 3. Retourne le template
     * 
     * @param model le modèle
     * @return le template warehouse/supplier-orders-to-receive.html
     */
    @GetMapping("/supplier-orders/to-receive")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    public String getSupplierOrdersToReceive(Model model) {
        List<SupplierOrder> orderedSupplierOrders = supplierOrderRepository.findByStatus(SupplierOrderStatus.ORDERED);
        
        List<SupplierOrderDto> supplierOrderDtos = orderedSupplierOrders.stream()
                .map(this::mapSupplierOrderToDto)
                .collect(Collectors.toList());
        
        model.addAttribute("supplierOrders", supplierOrderDtos);
        return "warehouse/supplier-orders-to-receive";
    }

    /**
     * Affiche les détails d'une commande fournisseur.
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param model le modèle
     * @return le template warehouse/supplier-order-detail.html
     */
    @GetMapping("/supplier-orders/{supplierOrderId}")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    public String getSupplierOrderDetail(@PathVariable Long supplierOrderId, Model model) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));
        
        SupplierOrderDto supplierOrderDto = mapSupplierOrderToDto(supplierOrder);
        model.addAttribute("supplierOrder", supplierOrderDto);
        return "warehouse/supplier-order-detail";
    }

    /**
     * Reçoit une commande fournisseur avec gestion du cas partiellement reçu (split).
     * 
     * Flux :
     * 1. Récupère les quantités reçues du formulaire (Map)
     * 2. Appelle SupplierOrderService.receiveSupplierOrder() qui implémente le split
     * 3. Si tout reçu : ORDERED → RECEIVED, stocks incrémentés
     * 4. Si partiellement reçu : split en 2 commandes (RECEIVED + ORDERED)
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param quantities Map<productId, quantityReceived> depuis le formulaire
     * @param attributes pour passer un message de succès
     * @return redirect vers /warehouse/supplier-orders/to-receive
     */
    @PostMapping("/supplier-orders/{supplierOrderId}/receive")
    @PreAuthorize("hasRole('ROLE_WAREHOUSEMAN')")
    @Transactional
    public String receiveSupplierOrder(
            @PathVariable Long supplierOrderId,
            @RequestParam(required = false) Map<String, String> quantities,
            RedirectAttributes attributes
    ) {
        try {
            // Convertir les quantités du formulaire en Map<Long, Integer>
            Map<Long, Integer> receivedQuantities = new HashMap<>();
            if (quantities != null) {
                quantities.forEach((key, value) -> {
                    if (key.startsWith("quantities[") && key.endsWith("]")) {
                        String productIdStr = key.substring("quantities[".length(), key.length() - 1);
                        try {
                            Long productId = Long.parseLong(productIdStr);
                            Integer qty = Integer.parseInt(value);
                            receivedQuantities.put(productId, qty);
                        } catch (NumberFormatException e) {
                            // Ignorer les conversions invalides
                        }
                    }
                });
            }

            // Appeler le service de réception (split logic)
            supplierOrderService.receiveSupplierOrder(supplierOrderId, receivedQuantities);

            // Récupérer la commande et incrémenter les stocks pour chaque ligne reçue
            // Attention : si split, il y a maintenant 2 commandes (RECEIVED + ORDERED)
            // On incrémente stocks UNIQUEMENT pour la commande RECEIVED
            SupplierOrder receivedOrder = supplierOrderRepository.findById(supplierOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));

            List<SupplierOrderLine> lines = supplierOrderLineRepository.findBySupplierOrderId(supplierOrderId);
            for (SupplierOrderLine line : lines) {
                if (receivedOrder.getStatus() == SupplierOrderStatus.RECEIVED) {
                    stockService.incrementStock(
                            line.getProduct().getId(),
                            line.getQuantity(),
                            receivedOrder
                    );
                }
            }

            attributes.addFlashAttribute("success", "Commande fournisseur reçue et stocks mis à jour");
            return "redirect:/warehouse/supplier-orders/to-receive";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/warehouse/supplier-orders/" + supplierOrderId;
        }
    }

    /**
     * Mappe une entité Order en OrderDetailsDto avec ses lignes.
     * 
     * @param order l'entité Order
     * @return le DTO pour affichage
     */
    private OrderDetailsDto mapOrderToDetailsDto(Order order) {
        List<OrderLineDto> lineDtos = orderLineRepository.findByOrderId(order.getId()).stream()
                .map(OrderLineDto::fromOrderLine)
                .collect(Collectors.toList());

        return OrderDetailsDto.fromOrder(order, lineDtos);
    }

    /**
     * Mappe une entité SupplierOrder en SupplierOrderDto avec ses lignes.
     * 
     * @param supplierOrder l'entité SupplierOrder
     * @return le DTO pour affichage
     */
    private SupplierOrderDto mapSupplierOrderToDto(SupplierOrder supplierOrder) {
        List<SupplierOrderLineDto> lineDtos = supplierOrderLineRepository.findBySupplierOrderId(supplierOrder.getId())
                .stream()
                .map(SupplierOrderLineDto::fromSupplierOrderLine)
                .collect(Collectors.toList());

        return SupplierOrderDto.fromSupplierOrder(supplierOrder, lineDtos);
    }
}
