package be.bstorm.tf_java_2026_introspringmvc.controllers;

import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrder;
import be.bstorm.tf_java_2026_introspringmvc.entities.SupplierOrderLine;
import be.bstorm.tf_java_2026_introspringmvc.enums.SupplierOrderStatus;
import be.bstorm.tf_java_2026_introspringmvc.models.supplierorder.SupplierOrderDto;
import be.bstorm.tf_java_2026_introspringmvc.models.supplierorder.SupplierOrderLineDto;
import be.bstorm.tf_java_2026_introspringmvc.repositories.ProductRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderLineRepository;
import be.bstorm.tf_java_2026_introspringmvc.repositories.SupplierOrderRepository;
import be.bstorm.tf_java_2026_introspringmvc.services.SupplierOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des commandes fournisseur (rôle ROLE_DEPARTMENT_HEAD).
 * 
 * Responsabilités :
 * - Afficher les commandes fournisseur en brouillon (DRAFT) à éditer
 * - Éditer les quantités d'une commande fournisseur (tant qu'en DRAFT)
 * - Soumettre une commande fournisseur (DRAFT → ORDERED, immuable)
 * - Afficher les détails d'une commande fournisseur
 * 
 * Flux de vie d'une SupplierOrder :
 * 1. DRAFT (créée automatiquement par StockService ou manuellement ici)
 * 2. ORDERED (après soumission, plus d'édition possible)
 * 3. RECEIVED (réceptionnée par magasinier = stocks incrémentés)
 * 
 * Créations automatiques :
 * - OrderService.validateOrder() appelle StockService.decrementStock()
 * - Si stock < seuil → isUnderThreshold() = true (mais ne crée pas automatiquement)
 * - Le chef de rayon crée manuellement via createSupplierOrder() ici
 * 
 * Routes disponibles : /supplier-orders/...
 * Accès restreint : ROLE_DEPARTMENT_HEAD uniquement
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/supplier-orders")
public class SupplierOrderController {

    private final SupplierOrderService supplierOrderService;
    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierOrderLineRepository supplierOrderLineRepository;
    private final ProductRepository productRepository;

    /**
     * Affiche la liste des commandes fournisseur en brouillon (DRAFT).
     * 
     * Flux :
     * 1. Récupère toutes les SupplierOrder avec statut DRAFT
     * 2. Mappe en SupplierOrderDto
     * 3. Retourne le template avec la liste
     * 
     * @param model le modèle
     * @return le template supplier-orders/draft.html
     */
    @GetMapping("/draft")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String getDraftSupplierOrders(Model model) {
        List<SupplierOrder> draftOrders = supplierOrderRepository.findByStatus(SupplierOrderStatus.DRAFT);
        
        List<SupplierOrderDto> supplierOrderDtos = draftOrders.stream()
                .map(this::mapSupplierOrderToDto)
                .collect(Collectors.toList());
        
        model.addAttribute("supplierOrders", supplierOrderDtos);
        return "supplier-orders/draft";
    }

    /**
     * Affiche les détails complets d'une commande fournisseur.
     * 
     * Flux :
     * 1. Récupère la commande par ID
     * 2. Mappe en SupplierOrderDto avec ses lignes
     * 3. Ajoute au modèle
     * 4. Si DRAFT : ajoute la liste de tous les produits pour l'ajout de lignes
     * 5. Retourne le template
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param model le modèle
     * @return le template supplier-orders/detail.html
     */
    @GetMapping("/{supplierOrderId}")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String getSupplierOrderDetail(
            @PathVariable Long supplierOrderId,
            Model model
    ) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));
        
        SupplierOrderDto supplierOrderDto = mapSupplierOrderToDto(supplierOrder);
        model.addAttribute("supplierOrder", supplierOrderDto);
        
        // Afficher si l'édition est possible (statut DRAFT)
        model.addAttribute("isEditable", supplierOrder.getStatus().equals(SupplierOrderStatus.DRAFT));
        
        // Passer la liste des produits pour le formulaire d'ajout de ligne (si DRAFT)
        if (supplierOrder.getStatus().equals(SupplierOrderStatus.DRAFT)) {
            model.addAttribute("allProducts", productRepository.findAll().stream()
                    .map(p -> new ProductOption(p.getId(), p.getName()))
                    .collect(java.util.stream.Collectors.toList()));
        }
        
        return "supplier-orders/detail";
    }

    /**
     * Crée manuellement une nouvelle commande fournisseur en brouillon (DRAFT).
     * 
     * Ceci est utilisé par le chef de rayon pour créer une commande manuellement.
     * Les commandes peuvent aussi être créées automatiquement par StockService
     * quand un stock tombe sous son seuil (via WarehouseController.validateOrder).
     * 
     * Flux :
     * 1. Crée une SupplierOrder vide (DRAFT)
     * 2. Redirige vers la page de détails pour éditer les lignes
     * 
     * Note : L'ajout de lignes se fait via updateLineQuantity() ou un formulaire dédié.
     * 
     * @param attributes pour passer un message de succès
     * @return redirect vers /supplier-orders/{supplierOrderId}
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String createSupplierOrder(RedirectAttributes attributes) {
        SupplierOrder supplierOrder = supplierOrderService.createSupplierOrder(1L, 0);
        
        attributes.addFlashAttribute("success", 
            "Nouvelle commande fournisseur créée (brouillon). ID : " + supplierOrder.getId());
        return "redirect:/supplier-orders/" + supplierOrder.getId();
    }

    /**
     * Endpoint unique pour traiter les modifications du formulaire update.
     * Peut être 2 actions :
     * 1. Si deleteProductId est présent : supprime la ligne
     * 2. Sinon : met à jour les quantités
     * 
     * @param supplierOrderId l'ID de la commande
     * @param quantities Map<productId, quantity> pour mise à jour
     * @param deleteProductId productId à supprimer (si présent)
     * @param attributes pour messages
     * @return redirect vers détail
     */
    @PostMapping("/{supplierOrderId}/update")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String updateSupplierOrder(
            @PathVariable Long supplierOrderId,
            @RequestParam(required = false) Map<String, String> quantities,
            @RequestParam(required = false) Long deleteProductId,
            RedirectAttributes attributes
    ) {
        try {
            if (deleteProductId != null) {
                // Supprimer la ligne
                supplierOrderService.deleteLineByProduct(supplierOrderId, deleteProductId);
                attributes.addFlashAttribute("success", "Produit supprimé");
            } else if (quantities != null && !quantities.isEmpty()) {
                // Mettre à jour les quantités
                quantities.forEach((key, value) -> {
                    if (key.startsWith("quantities[") && key.endsWith("]")) {
                        String productIdStr = key.substring("quantities[".length(), key.length() - 1);
                        try {
                            Long productId = Long.parseLong(productIdStr);
                            Integer qty = Integer.parseInt(value);
                            if (qty > 0) {
                                supplierOrderService.updateLineQuantity(supplierOrderId, productId, qty);
                            }
                        } catch (NumberFormatException e) {
                            // Ignorer les conversions invalides
                        }
                    }
                });
                attributes.addFlashAttribute("success", "Quantités mises à jour");
            }
            return "redirect:/supplier-orders/" + supplierOrderId;
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
    }

    /**
     * Édite la quantité d'une ligne de commande fournisseur.
     * 
     * Restrictions :
     * - La commande doit être en statut DRAFT (non soumise)
     * - Une fois ORDERED, plus d'édition possible
     * 
     * Flux :
     * 1. Récupère la SupplierOrder
     * 2. Vérifie que le statut est DRAFT
     * 3. Appelle SupplierOrderService.updateLineQuantity()
     * 4. Redirige vers les détails
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param productId l'ID du produit à modifier
     * @param quantity la nouvelle quantité (formulaire POST)
     * @param attributes pour passer un message
     * @return redirect vers /supplier-orders/{supplierOrderId}
     */
    @PostMapping("/{supplierOrderId}/lines/{productId}/quantity")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String updateLineQuantity(
            @PathVariable Long supplierOrderId,
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            RedirectAttributes attributes
    ) {
        try {
            if (quantity <= 0) {
                attributes.addFlashAttribute("error", "La quantité doit être strictement positive");
                return "redirect:/supplier-orders/" + supplierOrderId;
            }

            supplierOrderService.updateLineQuantity(supplierOrderId, productId, quantity);
            attributes.addFlashAttribute("success", "Quantité mise à jour");
            return "redirect:/supplier-orders/" + supplierOrderId;
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
    }

    /**
     * Supprime une ligne d'une commande fournisseur.
     * 
     * Restrictions :
     * - La commande doit être en statut DRAFT (non soumise)
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param productId l'ID du produit à supprimer
     * @param attributes pour passer un message
     * @return redirect vers /supplier-orders/{supplierOrderId}
     */
    @PostMapping("/{supplierOrderId}/lines/{productId}/delete")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String deleteLineByProduct(
            @PathVariable Long supplierOrderId,
            @PathVariable Long productId,
            RedirectAttributes attributes
    ) {
        try {
            supplierOrderService.deleteLineByProduct(supplierOrderId, productId);
            attributes.addFlashAttribute("success", "Ligne supprimée");
            return "redirect:/supplier-orders/" + supplierOrderId;
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
    }

    /**
     * Ajoute une nouvelle ligne à une commande fournisseur.
     * 
     * Restrictions :
     * - La commande doit être en statut DRAFT (non soumise)
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param productId l'ID du produit à ajouter
     * @param quantity la quantité à commander
     * @param attributes pour passer un message
     * @return redirect vers /supplier-orders/{supplierOrderId}
     */
    @PostMapping("/{supplierOrderId}/add-line")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String addLine(
            @PathVariable Long supplierOrderId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            RedirectAttributes attributes
    ) {
        try {
            if (quantity <= 0) {
                attributes.addFlashAttribute("error", "La quantité doit être strictement positive");
                return "redirect:/supplier-orders/" + supplierOrderId;
            }

            supplierOrderService.addLine(supplierOrderId, productId, quantity);
            attributes.addFlashAttribute("success", "Produit ajouté à la commande");
            return "redirect:/supplier-orders/" + supplierOrderId;
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
    }

    /**
     * Soumet une commande fournisseur (DRAFT → ORDERED).
     * 
     * Restrictions :
     * - La commande doit être en statut DRAFT
     * - Une fois soumise, la commande devient immuable (pas d'édition)
     * - Seuls les magasiniers peuvent recevoir la commande après (WarehouseController)
     * 
     * Flux :
     * 1. Récupère la SupplierOrder
     * 2. Vérifie le statut DRAFT
     * 3. Appelle SupplierOrderService.submitSupplierOrder()
     * 4. Redirige vers /supplier-orders/draft
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param attributes pour passer un message de succès
     * @return redirect vers /supplier-orders/draft
     */
    @PostMapping("/{supplierOrderId}/submit")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String submitSupplierOrder(
            @PathVariable Long supplierOrderId,
            RedirectAttributes attributes
    ) {
        SupplierOrder supplierOrder = supplierOrderRepository.findById(supplierOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande fournisseur non trouvée : " + supplierOrderId));
        
        // Vérifier que la commande peut être soumise
        if (!supplierOrder.getStatus().equals(SupplierOrderStatus.DRAFT)) {
            attributes.addFlashAttribute("error", 
                "Impossible de soumettre une commande qui n'est pas en brouillon (statut : " + supplierOrder.getStatus() + ")");
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
        
        // Vérifier que la commande a au moins une ligne
        List<SupplierOrderLine> lines = supplierOrderLineRepository.findBySupplierOrderId(supplierOrderId);
        if (lines.isEmpty()) {
            attributes.addFlashAttribute("error", "Impossible de soumettre une commande sans lignes");
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
        
        supplierOrderService.submitSupplierOrder(supplierOrderId);
        
        attributes.addFlashAttribute("success", 
            "Commande fournisseur " + supplierOrderId + " soumise au fournisseur");
        return "redirect:/supplier-orders/draft";
    }

    /**
     * Supprime une commande fournisseur en brouillon.
     * 
     * Restrictions :
     * - La commande doit être en statut DRAFT
     * - Supprime aussi toutes les lignes associées
     * 
     * Flux :
     * 1. Appelle SupplierOrderService.deleteSupplierOrder()
     * 2. Redirige vers /supplier-orders/draft
     * 
     * @param supplierOrderId l'ID de la commande fournisseur
     * @param attributes pour passer un message de succès
     * @return redirect vers /supplier-orders/draft
     */
    @PostMapping("/{supplierOrderId}/delete")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_HEAD')")
    public String deleteSupplierOrder(
            @PathVariable Long supplierOrderId,
            RedirectAttributes attributes
    ) {
        try {
            supplierOrderService.deleteSupplierOrder(supplierOrderId);
            attributes.addFlashAttribute("success", 
                "Commande fournisseur " + supplierOrderId + " supprimée");
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/supplier-orders/" + supplierOrderId;
        }
        
        return "redirect:/supplier-orders/draft";
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

    /**
     * Record simple pour passer les produits au template.
     */
    public record ProductOption(Long id, String name) {}
}
