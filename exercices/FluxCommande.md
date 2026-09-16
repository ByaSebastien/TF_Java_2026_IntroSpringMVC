### Exercice : Flux d'une commande (Order)

**Objectif :** Transformer le panier validé en commande, mettre en place les rôles de magasinier et chef de rayon, et implémenter le flux complet de traitement d'une commande avec gestion des stocks.

---

## 1. Modèle de données

### a) Nouvelles entités et modifications

**Entité `Order`** (la commande cliente)
- Statuts possibles : `PENDING` (en attente), `SHIPPED` (envoyée), `CANCELLED` (annulée)
- Liée à un `User` (celui qui a passé la commande)
- Contient des `OrderLine` (articles de la commande)

**Entité `OrderLine`** (ligne de commande)
- Lié à une `Order` et un `Product`
- Quantité demandée
- Prix unitaire au moment de la commande (snapshot)

**Entité `StockMovement`** *(optionnel mais recommandé)*
- Type : `OUTGOING` (sortie, pour une commande), `INCOMING` (entrée, réception fournisseur)
- Lié à un `Product`, une quantité, et une date
- Référence optionnelle à une `Order` ou une `SupplierOrder`
- Permet de tracer l'historique des mouvements de stock

**Entité `SupplierOrder`** (commande fournisseur)
- Statuts : `DRAFT` (en cours de préparation), `ORDERED` (commandée), `RECEIVED` (reçue)
- Créée automatiquement quand un stock tombe sous son seuil
- Contient des `SupplierOrderLine` (articles commandés au fournisseur)

**Entité `SupplierOrderLine`**
- Lié à une `SupplierOrder` et un `Product`
- Quantité commandée (modifiable par le chef de rayon avant validation)

**Modification de `Stock`**
- Ajouter un champ `threshold` (seuil minimum)
- Quand `quantityAvailable` tombe en dessous → créer une `SupplierOrder` automatiquement

### b) Rôles utilisateur

Ajouter deux rôles à votre système de sécurité :
- **`ROLE_WAREHOUSEMAN`** (Magasinier) : valide les commandes et réceptions
- **`ROLE_DEPARTMENT_HEAD`** (Chef de rayon) : gère les commandes fournisseur

---

## 2. Fonctionnalités à développer

### Phase 1 : Validation du panier → Création d'une commande

**a) Bouton "Valider la commande"**

- Un bouton sur la page du panier pour le client valide la commande
- Cette action :
  1. Crée une nouvelle `Order` avec le statut `PENDING`
  2. Copie les lignes du `CartLine` vers des `OrderLine`
  3. **Vide le panier** du client
  4. Redirige vers une confirmation

**b) Avantage pédagogique**
- Explique pourquoi on duplique les données : le panier peut changer, mais une commande doit conserver un snapshot des prix/articles au moment de la validation

---

### Phase 2 : Vue magasinier — Valider les commandes

**a) Écran de liste (Magasinier)**

- Liste toutes les commandes avec statut `PENDING`
- Affiche : client, date, montant total, nombre d'articles
- Un bouton pour accéder au détail et valider

**b) Écran de détail**

- Affiche tous les articles de la commande avec leur quantité demandée
- Un formulaire avec pour chaque ligne : **un champ numérique** pour indiquer la quantité réellement disponible
- Deux boutons : `✅ Valider l'expédition` ou `❌ Annuler la commande`

**c) Logique de validation**

**Cas 1 : Tout est disponible** (quantité fournie = quantité demandée pour chaque ligne)
- La commande passe au statut `SHIPPED`
- Décrémente les stocks avec chaque `Product` (via `Stock.quantityAvailable`)
- *(Optionnel)* Crée des `StockMovement` de type `OUTGOING`

**Cas 2 : Partiellement disponible** (au moins une ligne a une quantité < quantité demandée)
- La commande initiale est passée au statut `CANCELLED`
- **Commande 1** (ce qui est envoyé) : statut `SHIPPED`, avec les lignes disponibles
- **Commande 2** (ce qui manque) : statut `PENDING`, avec les lignes manquantes
- Décrémente les stocks **uniquement pour ce qui a été expédié**
- Redirige le magasinier vers une confirmation

**d) Vérification des stocks bas**

Après la décrémentation du stock, pour chaque `Product` :
- Si `quantityAvailable < threshold` → Créer automatiquement une `SupplierOrder` en statut `DRAFT`
  - Pré-remplir avec une quantité par défaut (ex : `threshold * 2`)
  - Ajouter des `SupplierOrderLine` correspondantes

---

### Phase 3 : Vue chef de rayon — Gérer les commandes fournisseur

**a) Écran de liste (Chef de rayon)**

- Liste toutes les `SupplierOrder` avec statut `DRAFT` ou `ORDERED`
- Affiche : date création, nombre d'articles, montant estimé
- Distinction visuelle entre `DRAFT` (en cours de préparation) et `ORDERED` (déjà passée)

**b) Écran de détail**

- Affiche tous les articles de la commande fournisseur
- Pour chaque ligne en statut `DRAFT` :
  - Champ numérique pour modifier la quantité
  - Bouton `❌` pour supprimer la ligne
- Possibilité d'ajouter de nouvelles lignes *(optionnel)*
- Deux boutons :
  - `➕ Ajouter un article` *(optionnel)*
  - `✅ Passer la commande` (passe au statut `ORDERED`)
- Bouton `Annuler` pour supprimer la commande

**c) Contraintes**
- Une `SupplierOrder` en statut `ORDERED` est **immuable** (pas de modification possible)

---

### Phase 4 : Vue magasinier — Valider les réceptions

**a) Écran de liste (Magasinier)**

- Liste toutes les `SupplierOrder` avec statut `ORDERED`
- Affiche : date, fournisseur (optionnel), nombre d'articles
- Un bouton pour accéder au détail et valider la réception

**b) Écran de détail**

- Affiche tous les articles commandés au fournisseur
- Pour chaque ligne :
  - Affiche la quantité commandée
  - Un champ numérique pour indiquer la quantité **réellement reçue**
- Deux boutons : `✅ Valider la réception` ou `❌ Refuser la réception`

**c) Logique de validation (similaire aux commandes clients)**

**Cas 1 : Tout est reçu** (quantité reçue = quantité commandée)
- La `SupplierOrder` passe au statut `RECEIVED`
- Incrémente les stocks avec chaque `Product`
- *(Optionnel)* Crée des `StockMovement` de type `INCOMING`

**Cas 2 : Partiellement reçu**
- La `SupplierOrder` initiale passe au statut `CANCELLED`
- **SupplierOrder 1** (reçu) : statut `RECEIVED`, avec les lignes reçues
- **SupplierOrder 2** (manquant) : statut `ORDERED`, avec les lignes manquantes
- Incrémente les stocks **uniquement pour ce qui a été reçu**
- Notification au chef de rayon des articles manquants

---

## 3. Flux complet — Vue d'ensemble

```
1. CLIENT : Valide son panier
   ↓
2. CRÉATION : Une Order est créée avec statut PENDING
   ↓
3. MAGASINIER : Accède à la liste des commandes PENDING
   ↓
4. MAGASINIER : Valide le détail de la commande (OK / Partiellement)
   ↓
   ├─ Si OK :
   │  ├─ Order → SHIPPED
   │  ├─ Stocks décrémentés
   │  └─ Vérification : Stock < Threshold ?
   │     └─ Si oui : Créer SupplierOrder en DRAFT
   │
   └─ Si Partiellement :
      ├─ Order initiale → CANCELLED
      ├─ Order 1 (expédié) → SHIPPED
      ├─ Order 2 (manquant) → PENDING
      └─ Stocks décrémentés partiellement
   ↓
5. CHEF DE RAYON : Accède à la liste des SupplierOrder en DRAFT
   ↓
6. CHEF DE RAYON : Modifie les quantités et valide
   ↓
7. SupplierOrder → ORDERED
   ↓
8. MAGASINIER : Accède à la liste des SupplierOrder en ORDERED
   ↓
9. MAGASINIER : Valide la réception (OK / Partiellement)
   ↓
   ├─ Si OK :
   │  ├─ SupplierOrder → RECEIVED
   │  └─ Stocks incrémentés
   │
   └─ Si Partiellement :
      ├─ SupplierOrder initiale → CANCELLED
      ├─ SupplierOrder 1 (reçu) → RECEIVED
      ├─ SupplierOrder 2 (manquant) → ORDERED
      └─ Stocks incrémentés partiellement
```

---

## 4. Points clés à respecter

✅ **Séparation des responsabilités**
- Service métier : validation de la commande, calcul des stocks, création automatique de commandes fournisseur
- Repository : persistance des entités
- Controller : validation HTTP, redirection, messages utilisateur

✅ **Transactions**
- Les opérations critiques (création Order + vidage panier, ou décrément stock + création SupplierOrder) doivent être atomiques

✅ **Validation des données**
- Les quantités fournies par le magasinier/chef de rayon ne doivent pas être négatives
- Une `SupplierOrder` en `ORDERED` n'est jamais modifiable

✅ **Sécurité**
- Seul un magasinier peut valider une commande
- Seul un chef de rayon peut modifier une `SupplierOrder` en `DRAFT`
- Seul un magasinier peut valider une réception

✅ **Traçabilité** *(avec StockMovement)*
- Chaque mouvement de stock doit être enregistré avec type, date, et référence à sa source (Order ou SupplierOrder)

---

## 5. Étapes suggérées

1. **Créer les entités** : Order, OrderLine, SupplierOrder, SupplierOrderLine, StockMovement (optionnel)
2. **Ajouter les rôles** : ROLE_WAREHOUSEMAN, ROLE_DEPARTMENT_HEAD
3. **Implémenter Phase 1** : Bouton panier → Order (simple et fondamental)
4. **Implémenter Phase 2** : Magasinier valide les commandes (cœur du flux)
5. **Implémenter Phase 3** : Chef de rayon gère les fournisseurs
6. **Implémenter Phase 4** : Magasinier valide les réceptions
7. **Bonus** : Ajouter des notifications, historique complet, statistiques de stock

---

## 6. Exemples à illustrer dans les vues

**Exemple 1 : Commande partiellement disponible**
```
Commande #1001 - Client : Jean Dupont

Articles demandés :
- Produit A × 10 → Disponible : 8 ✅
- Produit B × 5  → Disponible : 5 ✅
- Produit C × 3  → Disponible : 0 ❌

Résultat après validation du magasinier :
- Order #1001 → CANCELLED
- Order #1002 → SHIPPED (A:8, B:5) ✅
- Order #1003 → PENDING (C:3) ⏳
- Stock C < Seuil → SupplierOrder créée automatiquement
```

**Exemple 2 : Réception partielle fournisseur**
```
SupplierOrder #SO-001 - Fournisseur : XXX

Articles commandés :
- Produit A × 20 → Reçu : 20 ✅
- Produit C × 10 → Reçu : 7  ⚠️

Résultat après validation du magasinier :
- SupplierOrder #SO-001 → CANCELLED
- SupplierOrder #SO-002 → RECEIVED (A:20) ✅
- SupplierOrder #SO-003 → ORDERED (C:3) ⏳
```

---

## 7. Contrainte temporaire

Si l'authentification par rôles n'est pas encore implémentée :
- Créer des utilisateurs de test en base : un WAREHOUSEMAN et un DEPARTMENT_HEAD
- Ajouter des paramètres URL de test ou utiliser la session pour "simuler" le rôle
- Ce point sera revu une fois que `@PreAuthorize` sera pleinement fonctionnel
