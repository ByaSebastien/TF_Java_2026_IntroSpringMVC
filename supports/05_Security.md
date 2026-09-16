# 🔐 Spring Security - Protéger votre Application

Un guide complet pour comprendre **comment Spring Security gère l'authentification et l'autorisation**.

---

## 📋 Table des matières
1. [Concepts fondamentaux](#concepts)
2. [Les briques de base](#briques)
3. [Configuration avec SecurityConfig](#config)
4. [Flux de login/logout](#flux)
5. [États d'utilisateur et permissions](#etats)
6. [Utilisation avec @PreAuthorize et @AuthenticationPrincipal](#utilisation)

---

## 🤔 Concepts fondamentaux {#concepts}

### Authentification vs Autorisation

```mermaid
graph LR
    A["🔐 AUTHENTIFICATION<br/>Qui êtes-vous ?"] -->|"Je m'appelle Sébastien"| B["Vérifier l'identité<br/>Username + Password"]
    B -->|"✅ Identité confirmée"| C["Vous êtes Sébastien"]
    
    C -->|Maintenant| D["🛡️ AUTORISATION<br/>Qu'avez-vous le droit de faire ?"]
    D -->|"Vérifier les permissions"| E["Avez-vous le role ADMIN ?"]
    E -->|Oui| F["✅ Accès autorisé"]
    E -->|Non| G["❌ Accès refusé"]
    
    style A fill:#0277BD,color:#fff
    style B fill:#0277BD,color:#fff
    style C fill:#388E3C,color:#fff
    style D fill:#E65100,color:#fff
    style E fill:#E65100,color:#fff
    style F fill:#388E3C,color:#fff
    style G fill:#C62828,color:#fff
```

**Résumé** :
- **Authentification** = Vérifier que vous êtes bien qui vous prétendez être
- **Autorisation** = Vérifier que vous avez le droit d'accéder à cette ressource

---

## 🏗️ Les briques de base {#briques}

### 1️⃣ **UserDetails** : Qui est l'utilisateur ?

```java
// UserDetails est une interface Spring Security
// Votre entité User DOIT l'implémenter

@Entity
public class User extends BaseEntity implements UserDetails {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;  // ← Doit être HACHÉE !
    
    @Enumerated(EnumType.STRING)
    private UserRole role;  // ADMIN, USER, etc.
    
    // ✅ Implémentation de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourner les permissions (roles)
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
    
    // Les autres méthodes par défaut retournent true
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return true; }
}
```

**UserDetails, c'est le contrat** : "Spring, voici les infos de cet utilisateur"

### 2️⃣ **UserDetailsService** : Comment charger l'utilisateur ?

```java
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Spring appelle cette méthode pour charger un utilisateur par son username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Chercher l'utilisateur en BD
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User with username " + username + " not found"
                ));
    }
}
```

**UserDetailsService, c'est la logique** : "Spring, pour récupérer un utilisateur, fais ceci"

### 3️⃣ **SecurityConfig** : Comment configurer Spring Security ?

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← Active @PreAuthorize sur les méthodes
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt = hachage sécurisé des mots de passe
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuration des routes et permissions
            .authorizeHttpRequests(r -> r
                .requestMatchers("/login").anonymous()  // Seulement anonymes
                .requestMatchers("/logout").authenticated()  // Seulement authentifiés
                .anyRequest().permitAll()  // Le reste = accessible à tous
            )
            // Configuration du login
            .formLogin(c -> c
                .loginPage("/login")  // Page de login personnalisée
                .permitAll()  // La page de login doit être accessible
                .defaultSuccessUrl("/?logged", true)  // Redirection après login
                .failureUrl("/login?error")  // Redirection si erreur
            )
            // Configuration du logout
            .logout(c -> c
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login?logout")
            );
        
        return http.build();
    }
}
```

---

## 🔧 Configuration avec SecurityConfig {#config}

### Comprendre les routes dans authorizeHttpRequests

```mermaid
graph TD
    A["Utilisateur accède à une URL"] --> B{Quelle URL ?}
    
    B -->|/login| C{Quelle requête ?}
    C -->|GET| D["Pas encore authentifié<br/>anonymous()"]
    D --> E["✅ Autorisé<br/>Affiche le formulaire"]
    C -->|POST| F["Essaie de s'authentifier"]
    F --> G{Auth réussie ?}
    G -->|Oui| H["✅ Authentifié"]
    G -->|Non| I["❌ Redirection login?error"]
    
    B -->|/logout| J{Authentifié ?}
    J -->|Oui| K["✅ Autorisé<br/>Déconnexion"]
    J -->|Non| L["❌ Erreur 403 Forbidden"]
    
    B -->|Autre URL| M["Vérifier avec anyRequest"]
    M --> N["permitAll<br/>= Accessible à tous"]
    
    style E fill:#388E3C,color:#fff
    style H fill:#388E3C,color:#fff
    style K fill:#388E3C,color:#fff
    style L fill:#C62828,color:#fff
    style N fill:#0277BD,color:#fff
```

### Pourquoi `.anonymous()` et `.permitAll()` ?

```
❌ MAUVAIS : /login seulement accessible aux anonymes
────────────────────────────────────────────────────

Utilisateur authentifié tente d'accéder à /login
    ↓
Spring refuse (pas anonymous)
    ↓
L'utilisateur ne peut pas voir le formulaire
    ↓
❌ IMPOSSIBLE de réutiliser le formulaire pour changer de compte
```

```
✅ BON : Utiliser PreAuthorize dans le Controller
────────────────────────────────────────────────────

Route /login : permitAll()
    ↓
Utilisateur authentifié peut y accéder
    ↓
Dans le Controller :
@GetMapping("/login")
@PreAuthorize("isAnonymous() OR hasAuthority('ADMIN')")
public String login() { ... }
    ↓
✅ Plus flexible et cohérent
```

### Le flux de SecurityConfig

```mermaid
sequenceDiagram
    participant Client as 👤 Client
    participant Spring as 🔐 Spring Security
    participant Config as ⚙️ SecurityConfig
    participant Auth as 🔑 AuthService
    participant Repo as 📊 UserRepository
    participant DB as 🗄️ BD
    
    Note over Spring: Démarrage
    Spring->>Config: Initialiser la chaîne de filtres
    Config->>Config: authorizeHttpRequests configurée
    Config->>Config: formLogin/logout configurée
    Config->>Spring: ✅ SecurityFilterChain prêt
    
    Note over Client: Utilisateur arrive
    Client->>Spring: GET /product
    Spring->>Config: Cette route est-elle protégée ?
    Config->>Spring: Pas de restriction (permitAll)
    Spring->>Client: ✅ Affiche la page
```

---

## 🔐 Flux de login/logout {#flux}

### Flux de LOGIN complet

```mermaid
sequenceDiagram
    participant User as 👤 Utilisateur
    participant Browser as 🌐 Navigateur
    participant Controller as 🎮 Controller
    participant Security as 🔐 Spring Security
    participant AuthService as 🔑 AuthService
    participant UserRepo as 📊 UserRepository
    participant DB as 🗄️ PostgreSQL
    
    User->>Browser: Clique sur "Se connecter"
    Browser->>Controller: GET /login
    Controller->>Browser: Affiche formulaire
    Browser->>User: Montre le formulaire
    
    User->>Browser: Remplit username + password
    Browser->>Security: POST /login (username, password)
    
    Note over Security: ① Spring intercepte le login
    Security->>AuthService: loadUserByUsername(username)
    
    Note over AuthService: ② AuthService recherche l'user
    AuthService->>UserRepo: findUserByUsername(username)
    UserRepo->>DB: SELECT * FROM user_ WHERE username=?
    DB-->>UserRepo: User trouvé (avec password hachée)
    UserRepo-->>AuthService: User
    AuthService-->>Security: UserDetails
    
    Note over Security: ③ Spring vérifie le password
    Security->>Security: BCrypt.matches(password, user.password)
    Security->>Security: ✅ Password correct !
    
    Note over Security: ④ Spring crée une session
    Security->>Security: Crée un SecurityContext
    Security->>Security: Génère JSESSIONID cookie
    
    Note over Security: ⑤ Redirection après succès
    Security-->>Browser: redirect:/? (logged=true)
    Browser->>Controller: GET /?logged
    Controller->>Browser: Affiche page accueil
    Browser->>User: Connecté ! 🎉
```

**Ce que SPRING fait pour vous** ✅ :
- ✅ Crée un formulaire de login
- ✅ Intercepte le POST /login
- ✅ Appelle `AuthService.loadUserByUsername()`
- ✅ Valide le password avec BCrypt
- ✅ Crée la session + cookie
- ✅ Redirige vers defaultSuccessUrl

**Ce que VOUS faites** 🎮 :
- 🎮 Implémenter `UserDetailsService.loadUserByUsername()`
- 🎮 Créer la page /login (formulaire HTML)
- 🎮 Configurer SecurityConfig

### Flux de LOGOUT complet

```mermaid
sequenceDiagram
    participant User as 👤 Utilisateur
    participant Browser as 🌐 Navigateur
    participant Controller as 🎮 Controller
    participant Security as 🔐 Spring Security
    participant Session as 💾 Session
    
    User->>Browser: Clique sur "Déconnexion"
    Browser->>Controller: GET /logout
    
    Note over Security: ① Spring intercepte /logout
    Security->>Security: Vérifie que l'user est authentifié
    
    alt Pas authentifié
        Security-->>Browser: ❌ 403 Forbidden
    else Authentifié
        Note over Security: ② Spring supprime la session
        Security->>Session: Invalide la session
        Security->>Session: Supprime JSESSIONID cookie
        
        Note over Security: ③ Redirection après logout
        Security-->>Browser: redirect:/login?logout
        Browser->>Controller: GET /login?logout
        Controller->>Browser: Affiche login avec message
        Browser->>User: Déconnecté ! Au revoir 👋
    end
```

**Ce que SPRING fait pour vous** ✅ :
- ✅ Intercepte /logout
- ✅ Vérifie que l'utilisateur est authentifié
- ✅ Invalide la session
- ✅ Supprime les cookies
- ✅ Redirige

**Ce que VOUS faites** 🎮 :
- 🎮 Ajouter un bouton "Déconnexion" dans le template
- 🎮 Configurer logoutUrl et logoutSuccessUrl

---

## 👥 États d'utilisateur et permissions {#etats}

### Les 3 états possibles

```mermaid
graph TD
    A["Vous arrivez sur le site"] --> B{Êtes-vous identifié ?}
    
    B -->|Non| C["🔓 ANONYMOUS<br/>Pas de session<br/>Pas d'identité"]
    C --> D["Vous pouvez :<br/>- Voir les produits<br/>- Accéder /login<br/>- Pas d'accès admin"]
    
    B -->|Oui| E["🔐 AUTHENTICATED<br/>Session active<br/>Vous êtes USER ou ADMIN"]
    E --> F{Quel role ?}
    
    F -->|USER| G["👤 Role USER<br/>Authority: USER<br/>Permissions: LIMITÉES"]
    G --> H["Vous pouvez :<br/>- Ajouter au panier<br/>- Voir votre compte<br/>- Pas d'admin"]
    
    F -->|ADMIN| I["👨‍💼 Role ADMIN<br/>Authority: ADMIN<br/>Permissions: TOUTES"]
    I --> J["Vous pouvez :<br/>- Créer produits<br/>- Modifier produits<br/>- Supprimer produits"]
    
    style C fill:#78909C,color:#fff
    style D fill:#78909C,color:#fff
    style G fill:#388E3C,color:#fff
    style H fill:#388E3C,color:#fff
    style I fill:#E65100,color:#fff
    style J fill:#E65100,color:#fff
```

### Comprendre les autorités (Authorities)

```java
// Dans l'entité User
@Enumerated(EnumType.STRING)
private UserRole role;  // ADMIN ou USER

// Dans User.getAuthorities()
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    // L'authority = le nom du rôle
    return List.of(new SimpleGrantedAuthority(role.name()));
    // Retourne : [ADMIN] ou [USER]
}
```

### Les conditions de sécurité

| Condition | Signification | Exemple |
|-----------|---|---|
| `isAnonymous()` | Pas authentifié | Visiteur qui regarde juste |
| `isAuthenticated()` | Authentifié (peu importe le role) | Utilisateur connecté |
| `hasAuthority('ADMIN')` | A le role ADMIN | Administrateur |
| `hasAuthority('USER')` | A le role USER | Utilisateur normal |
| `hasAnyAuthority('ADMIN', 'USER')` | A au moins un des roles | Tous les authentifiés |

---

## 🔑 Utilisation avec @PreAuthorize et @AuthenticationPrincipal {#utilisation}

### C'est LÀ que vous utilisez Security au quotidien !

```mermaid
graph LR
    A["🔐 Spring Security<br/>Mis en place UNE fois<br/>dans SecurityConfig"] -->|Utilisé| B["@PreAuthorize<br/>Sur chaque méthode"]
    A -->|Utilisé| C["@AuthenticationPrincipal<br/>Pour accéder à l'user"]
    
    style A fill:#6A1B9A,color:#fff
    style B fill:#E65100,color:#fff
    style C fill:#E65100,color:#fff
```

### 1️⃣ **@PreAuthorize** : Vérifier les permissions

```java
@Controller
public class ProductController {
    
    // ✅ SANS restriction : tout le monde peut voir
    @GetMapping
    public String index(Model model) {
        // Affiche tous les produits
        return "product/index";
    }
    
    // ✅ Réservé aux admins
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/create")
    public String create(Model model) {
        // Affiche le formulaire de création
        return "product/create";
    }
    
    // ✅ Seulement les admins peuvent créer
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public String createProduct(@ModelAttribute ProductForm form) {
        // Crée un produit
        return "redirect:/product";
    }
    
    // ✅ Réservé aux utilisateurs connectés (USER ou ADMIN)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId) {
        // Ajoute au panier
        return "redirect:/product";
    }
    
    // ✅ Réservé aux anonymes (pour le login)
    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String login() {
        // Affiche le formulaire de login
        return "login";
    }
}
```

### Qu'est-ce qui se passe si l'utilisateur n'a pas l'accès ?

```mermaid
graph TD
    A["Utilisateur accède à /product/create"] --> B["@PreAuthorize vérifie les permissions"]
    B --> C{Utilisateur a ADMIN ?}
    
    C -->|Non| D["❌ Accès refusé"]
    D --> E["Spring retourne 403 Forbidden"]
    E --> F["Ou redirige vers /login"]
    
    C -->|Oui| G["✅ Accès autorisé"]
    G --> H["La méthode s'exécute normalement"]
    
    style D fill:#C62828,color:#fff
    style G fill:#388E3C,color:#fff
```

### 2️⃣ **@AuthenticationPrincipal** : Accéder à l'utilisateur connecté

```java
@Controller
public class CartController {
    
    private final CartService cartService;
    
    // ✅ Récupérer l'utilisateur connecté
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/add/{productId}")
    public String addToCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user  // ← L'utilisateur connecté
    ) {
        // 'user' est l'User actuellement authentifié
        // C'est la même entité User de la BD
        
        cartService.addToCart(user, productId);
        return "redirect:/product";
    }
    
    // ✅ Utiliser le user pour checker les droits
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/wishlist/add/{productId}")
    public String addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user
    ) {
        // Vérifier que l'utilisateur a le droit
        if (!user.getRole().equals(UserRole.PREMIUM)) {
            throw new AccessDeniedException("Réservé aux utilisateurs PREMIUM");
        }
        
        // Ajouter à la wishlist
        user.addToWishlist(productRepository.findById(productId).orElseThrow());
        userRepository.save(user);
        
        return "redirect:/product";
    }
}
```

### Comment accéder à l'utilisateur anonyme ?

```java
@Controller
public class ProductController {
    
    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        // ⚠️ ATTENTION : user peut être null si anonyme !
        
        if (user != null) {
            // Utilisateur authentifié
            model.addAttribute("username", user.getUsername());
        } else {
            // Utilisateur anonyme
            model.addAttribute("username", "Visiteur");
        }
        
        return "product/index";
    }
}
```

### Exemples complets d'utilisation

```java
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final ProductService productService;
    
    // ✅ Seulement les ADMINs
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("adminName", admin.getUsername());
        return "admin/dashboard";
    }
    
    // ✅ Seulement les ADMINs (créer un produit)
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/products/create")
    public String createProduct(
            @ModelAttribute ProductForm form,
            @AuthenticationPrincipal User admin
    ) {
        productService.createProduct(form);
        // Log : admin.getUsername() a créé un produit
        return "redirect:/admin/products";
    }
    
    // ✅ Seulement les utilisateurs authentifiés
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "profile";
    }
    
    // ✅ Seulement les anonymes
    @PreAuthorize("isAnonymous()")
    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
```

---

## 📊 Résumé du flux complet

```mermaid
graph TD
    A["🔐 SETUP (une fois)"] --> B["1. User implements UserDetails"]
    B --> C["2. AuthService implements UserDetailsService"]
    C --> D["3. SecurityConfig avec @EnableWebSecurity"]
    
    D --> E["🔓 UTILISATION (partout, tout le temps)"]
    E --> F["@PreAuthorize sur les méthodes"]
    E --> G["@AuthenticationPrincipal pour l'user"]
    
    F --> H["Requête utilisateur"]
    G --> H
    H --> I["Spring Security intercepte"]
    I --> J{L'utilisateur a<br/>les permissions ?}
    
    J -->|Non| K["❌ 403 Forbidden"]
    J -->|Oui| L["✅ Exécute la méthode"]
    
    style A fill:#6A1B9A,color:#fff
    style E fill:#E65100,color:#fff
    style K fill:#C62828,color:#fff
    style L fill:#388E3C,color:#fff
```

### Les points clés à maîtriser

#### 🔧 Setup (une fois au démarrage)
1. `User implements UserDetails` : "Voici qui est l'utilisateur"
2. `AuthService implements UserDetailsService` : "Voici comment le charger"
3. `SecurityConfig` : "Voici les règles de sécurité"

#### 🎯 Utilisation (partout dans le code)
1. `@PreAuthorize("...")` : "Qui a le droit d'accéder ?"
2. `@AuthenticationPrincipal User user` : "Qui est l'utilisateur ?"

---

## ✨ Points à retenir

```mermaid
graph TB
    subgraph Setup["🔐 SETUP (Une fois)"]
        A["UserDetails<br/>Implémentation"]
        B["UserDetailsService<br/>Chargement user"]
        C["SecurityConfig<br/>Configuration routes"]
    end
    
    subgraph Usage["🎯 USAGE (Partout)"]
        D["@PreAuthorize<br/>Vérifier permissions"]
        E["@AuthenticationPrincipal<br/>Récupérer l'user"]
    end
    
    Setup -->|Donne accès à| Usage
    
    style A fill:#6A1B9A,color:#fff
    style B fill:#6A1B9A,color:#fff
    style C fill:#6A1B9A,color:#fff
    style D fill:#E65100,color:#fff
    style E fill:#E65100,color:#fff
```

### Les conditions les plus utilisées

```java
// Pour vérifier les permissions
@PreAuthorize("isAnonymous()")        // Visiteur non connecté
@PreAuthorize("isAuthenticated()")    // Connecté (peu importe le role)
@PreAuthorize("hasAuthority('ADMIN')") // Uniquement admins
@PreAuthorize("hasAuthority('USER')")  // Uniquement users
```

### Accéder à l'utilisateur

```java
// Dans une méthode de Controller
@PreAuthorize("isAuthenticated()")
public String maMethode(@AuthenticationPrincipal User user) {
    String username = user.getUsername();
    UserRole role = user.getRole();
    // ...
}

// L'utilisateur est null s'il est anonyme !
@GetMapping
public String index(@AuthenticationPrincipal User user) {
    if (user != null) {
        // Connecté
    } else {
        // Anonyme
    }
}
```

---

**Vous maîtrisez maintenant Spring Security ! 🚀**
