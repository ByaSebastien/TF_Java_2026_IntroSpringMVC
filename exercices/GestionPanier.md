
### Exercice : Gestion du panier (Cart)

**Objectif :** Adapter votre projet pour mettre en place un système de panier d'achat.

#### 1. Modèle de données

Créez (ou adaptez) les entités suivantes :

- **User** : l'utilisateur
- **Cart** : le panier, lié à un utilisateur
- **CartLine** : une ligne du panier (un article + sa quantité), liée à un panier

#### 2. Fonctionnalités à développer

**a) Ajout au panier**

- Un bouton permettant d'ajouter un article au panier
- Si l'article est déjà dans le panier, on incrémente simplement sa quantité (pas de doublon)

**b) Affichage du panier**

- Une vue listant le contenu du panier
- Des boutons **+** / **−** sur chaque ligne pour ajuster la quantité
- Si on clique sur **−** et que la quantité tombe à 0, la ligne est automatiquement supprimée
- Un bouton **Supprimer** pour retirer directement une ligne, peu importe la quantité
- Le **montant total** du panier doit être affiché

**c) Bonus — Lien dans la navigation**

- Ajouter un lien vers le panier dans la barre de navigation
- Afficher sur ce lien/bouton le **nombre d'articles** présents dans le panier (badge)

#### 3. Contrainte technique temporaire

La gestion des utilisateurs (authentification, etc.) n'est pas encore implémentée. En attendant :

- Créez en base de données **un utilisateur avec l'id `1`**
- Faites en sorte que toute la logique du panier fonctionne pour cet utilisateur fixe
- Ce point sera revu plus tard une fois la gestion des utilisateurs en place