package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.Order;
import be.bstorm.tf_java_2026_introspringmvc.enums.OrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.models.order.OrderDetailsDto;
import be.bstorm.tf_java_2026_introspringmvc.models.order.OrderLineDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.OrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.bstorm.tf_java_2026_introspringmvc.entities.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des commandes client.
 * 
 * Responsabilités :
 * - Créer une nouvelle commande à partir du panier (passage Cart → Order)
 * - Afficher l'historique des commandes de l'utilisateur
 * - Afficher les détails d'une commande
 * - Annuler une commande (si encore possible)
 * 
 * Accès : utilisateurs authentifiés avec rôle ROLE_USER
 * Routes disponibles : /orders/...
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;

    /**
     * Affiche l'historique des commandes de l'utilisateur actuellement connecté.
     * 
     * Flux :
     * 1. Récupère les commandes de l'utilisateur via OrderService
     * 2. Mappe chaque Order en OrderDetailsDto
     * 3. Ajoute la liste au modèle
     * 4. Retourne le template pour afficher l'historique
     * 
     * @param model le modèle pour passer les données à la vue
     * @param user l'utilisateur actuellement connecté
     * @return le template orders/my-orders.html
     */
    @GetMapping
    public String getMyOrders(Model model, @AuthenticationPrincipal User user) {
        List<Order> userOrders = orderService.getOrdersByUser(user.getId());
        
        List<OrderDetailsDto> orderDtos = userOrders.stream()
                .map(this::mapOrderToDetailsDto)
                .collect(Collectors.toList());
        
        model.addAttribute("orders", orderDtos);
        return "orders/my-orders";
    }

    /**
     * Affiche les détails complets d'une commande spécifique.
     * 
     * Sécurité : l'utilisateur ne peut consulter que ses propres commandes.
     * 
     * Flux :
     * 1. Récupère la commande par ID
     * 2. Vérifie que l'utilisateur connecté en est le propriétaire
     * 3. Mappe en OrderDetailsDto avec ses lignes
     * 4. Ajoute au modèle
     * 5. Retourne le template pour afficher les détails
     * 
     * @param orderId l'ID de la commande
     * @param model le modèle
     * @param user l'utilisateur actuellement connecté
     * @return le template orders/order-detail.html
     */
    @GetMapping("/{orderId}")
    public String getOrderDetail(
            @PathVariable Long orderId,
            Model model,
            @AuthenticationPrincipal User user
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));
        
        // Vérifier que l'utilisateur est propriétaire de la commande
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Accès refusé à cette commande");
        }
        
        OrderDetailsDto orderDto = mapOrderToDetailsDto(order);
        model.addAttribute("order", orderDto);
        return "orders/order-detail";
    }

    /**
     * Crée une nouvelle commande à partir du panier de l'utilisateur.
     * 
     * Flux :
     * 1. Appelle OrderService.createOrderFromCart(userId)
     *    - Récupère le panier
     *    - Crée une Order (PENDING)
     *    - Crée les OrderLines
     *    - Vide le panier
     * 2. Retourne la commande créée
     * 3. Redirige vers la page de détails
     * 
     * Gestion des erreurs :
     * - Panier vide → Message d'erreur + redirect vers panier
     * - Utilisateur introuvable → Erreur 500
     * 
     * @param user l'utilisateur actuellement connecté
     * @param attributes pour passer un message de succès/erreur
     * @return redirect vers /orders/{orderId} ou /cart si erreur
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String createOrder(
            @AuthenticationPrincipal User user,
            RedirectAttributes attributes
    ) {
        try {
            Order order = orderService.createOrderFromCart(user.getId());
            
            attributes.addFlashAttribute("success", 
                "✓ Commande créée avec succès ! Votre numéro de commande est : " + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", 
                "✗ Erreur : " + e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Annule une commande (passe le statut PENDING → CANCELLED).
     * 
     * Restrictions :
     * - Seul un utilisateur USER peut annuler ses propres commandes
     * - Une commande ne peut être annulée que si elle est encore PENDING
     * - Une fois SHIPPED ou CANCELLED, l'annulation n'est plus possible
     * 
     * Note : À ce stade du développement, le repositionnement en stocks n'est pas implémenté.
     * Si l'utilisateur annule après validation magasinier, les stocks ne sont pas restaurés.
     * Ceci est volontaire (simplification du flux) et serait à gérer en production.
     * 
     * @param orderId l'ID de la commande à annuler
     * @param user l'utilisateur actuellement connecté
     * @param attributes pour passer un message de succès/erreur
     * @return redirect vers /orders
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user,
            RedirectAttributes attributes
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée : " + orderId));
        
        // Vérifier propriété
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Accès refusé à cette commande");
        }
        
        // Vérifier que la commande peut être annulée
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            attributes.addFlashAttribute("error", 
                "Impossible d'annuler une commande qui n'est pas en attente (statut : " + order.getStatus() + ")");
            return "redirect:/orders/" + orderId;
        }
        
        orderService.cancelOrder(orderId);
        
        attributes.addFlashAttribute("success", "Commande " + orderId + " annulée");
        return "redirect:/orders";
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
}
