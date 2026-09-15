# 🔄 Les DTO et les Mappers - Les Traducteurs de votre Appli

Un guide pour comprendre **pourquoi il faut séparer les données du web et de la base de données**.

---

## 📋 Table des matières
1. [Le problème sans DTO](#probleme)
2. [C'est quoi un DTO ?](#dto)
3. [Les 3 cas d'usage majeurs](#casusage)
4. [Les Mappers : Convertir les données](#mappers)
5. [Implémentation dans notre projet](#implementation)
6. [MapStruct : Automatiser le mapping](#mapstruct)
7. [Exercices pratiques](#exercices)

---

## 😱 Le problème sans DTO {#probleme}

### Scénario : On envoie l'entité directement au client

Imaginons qu'on retourne l'entité `Product` telle quelle depuis la base de données :

```java
@GetMapping("/{id}")
public Product details(@PathVariable Long id) {
    return productRepository.findById(id).orElseThrow();  // ❌ Retourner l'entité directement
}
```

**Ça retourne au client** (via JSON) :
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "description": "Un super ordinateur",
  "imageUrl": "https://...",
  "categoryId": 5,
  "category": {
    "id": 5,
    "name": "Électronique"
  },
  "stock": {
    "id": 10,
    "quantity": 50,
    "warehouseLocation": "Zone A3",
    "lastRestocked": "2025-03-15T10:00:00",
    "supplier": "TechCorp Inc.",
    "supplierPhone": "+1-555-1234",
    "supplierEmail": "contact@techcorp.com"
  }
}
```

### 🚨 Quels problèmes ?

#### 1️⃣ **Fuite de données (sécurité)**
Le client reçoit des infos qu'il n'a pas besoin de connaître :
- Les détails du supplier (téléphone, email)
- L'emplacement exact en entrepôt

C'est **dangereux** !

#### 2️⃣ **Données non pertinentes**
L'API retourne trop d'informations. La page web n'en affiche que 5 sur 20.
- Consommation réseau inutile
- Plus lent
- Plus de données à traiter

#### 3️⃣ **Couplage fort**
Si on ajoute un champ à `Product`, le client le reçoit immédiatement.
Si on supprime un champ, l'API casse.
**L'API est directement liée à la structure de la BD !**

#### 4️⃣ **Plusieurs vues possibles**
On veut parfois afficher :
- La liste des produits (nom, prix, image)
- Un produit détaillé (+ description)
- Les ventes passées (que l'ID et le nom)

**Une seule entité** ne peut pas répondre à tous les besoins.

---

## ✅ C'est quoi un DTO ? {#dto}

### Définition simple

**DTO = Data Transfer Object**

C'est une classe qui contient **uniquement les données qu'on veut envoyer** à une couche externe (client web, API REST, etc).

```
BD (Entity) → DTO → Client
```

### Les règles d'or

1. ✅ **Contient uniquement les données utiles** (pas de sécurité sensible)
2. ✅ **Peut avoir une structure différente** de l'entité BD
3. ✅ **Immuable** (recommandé) ou très simple
4. ✅ **Pas de logique métier** (c'est juste de la data)

### Exemple : ProductIndexDto

Dans notre projet, quand on affiche une **liste de produits**, on ne veut que :
- ID
- Nom
- Prix
- Image
- Catégorie (simplement l'ID et le nom)

```java
public record ProductIndexDto(
        Long id,
        String name,
        double price,
        String imageUrl,
        CategoryDto category
) {
    // Mapper fromEntity : convertir une entité en DTO
    public static ProductIndexDto fromEntity(Product p) {
        return new ProductIndexDto(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryDto.fromEntity(p.getCategory())
        );
    }
}
```

**Ce que le client reçoit** :
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "imageUrl": "https://...",
  "category": {
    "id": 5,
    "name": "Électronique"
  }
}
```

**Les avantages** :
- ✅ Pas de `stock`, `supplierPhone`, etc.
- ✅ Structure claire et prévisible
- ✅ L'API ne change pas si on ajoute des champs à `Product`

---

## 🎯 Les 3 cas d'usage majeurs {#casusage}

### 1️⃣ **Output DTO** - Envoyer des données au client

**Quand ?** Le controller récupère une entité et veut l'envoyer via JSON/HTML

```
Repository → Entity Product → DTO ProductIndexDto → Client
```

**Exemple dans notre projet** :
```java
@GetMapping
public String index(Model model) {
    List<Product> products = productRepository.findAll();
    
    // Convertir les entités en DTOs
    List<ProductIndexDto> dtos = products.stream()
            .map(ProductIndexDto::fromEntity)
            .toList();
    
    model.addAttribute("products", dtos);
    return "product/index";
}
```

### 2️⃣ **Input DTO (Form)** - Recevoir les données du client

**Quand ?** Un formulaire HTML POST envoie des données, on veut les valider avant BD

```
Form HTML → ProductForm (DTO) → Validation → Entity Product → BD
```

**Exemple dans notre projet** :
```java
public class ProductForm {
    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private double price;

    @Size(max = 255)
    private String description;

    @NotNull
    @Min(1)
    private Long categoryId;

    // Mapper : convertir DTO en entité
    public Product toEntity() {
        return new Product(name, description, price, imageUrl, categoryId);
    }
}
```

**Comment l'utiliser** :
```java
@PostMapping("/create")
public String create(
    @ModelAttribute ProductForm form,
    BindingResult errors
) {
    if (errors.hasErrors()) {
        return "product/create";  // Erreurs de validation
    }
    
    Product product = form.toEntity();  // Convertir DTO → Entité
    productRepository.save(product);
    
    return "redirect:/product";
}
```

### 3️⃣ **Query/Filter DTO** - Paramètres de recherche

**Quand ?** On veut filtrer par plusieurs critères

```
GET /product/search?name=Laptop&minPrice=500&maxPrice=1500&categoryId=5
        ↓
ProductFilter (DTO) → Repository query → Results
```

**Exemple dans notre projet** :
```java
public record ProductFilter(
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId
) {
}
```

**Utilisation** :
```java
@GetMapping("/search")
public String search(
    @ModelAttribute ProductFilter filter,
    Model model
) {
    List<Product> products = productRepository.findByFilter(filter);
    model.addAttribute("products", products);
    return "product/index";
}
```

---

## 🔀 Les Mappers : Convertir les données {#mappers}

### Qu'est-ce qu'un Mapper ?

Un **Mapper** est une fonction (ou classe) qui **convertit une entité en DTO** (ou vice versa).

### 3 approches

#### 1️⃣ **Méthode statique dans le DTO** (Simple)

C'est ce qu'on fait dans notre projet :

```java
public record ProductIndexDto(...) {
    public static ProductIndexDto fromEntity(Product p) {
        return new ProductIndexDto(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryDto.fromEntity(p.getCategory())
        );
    }
}
```

**Pros** :
- ✅ Simple et lisible
- ✅ Pas de dépendance externe
- ✅ Facile à debugger

**Cons** :
- ❌ Code répétitif si beaucoup de DTOs
- ❌ À faire manuellement

#### 2️⃣ **Classe Mapper dédiée** (Organisé)

Créer une classe qui centralise les mappers :

```java
@Component
public class ProductMapper {
    
    public ProductIndexDto toDto(Product product) {
        return new ProductIndexDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                categoryToDto(product.getCategory())
        );
    }
    
    public Product toEntity(ProductForm form, Category category) {
        return new Product(
                form.getName(),
                form.getDescription(),
                form.getPrice(),
                form.getImageUrl(),
                category
        );
    }
    
    private CategoryDto categoryToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
```

**Utilisation** :
```java
@GetMapping
public String index(Model model) {
    List<Product> products = productRepository.findAll();
    
    // Mapper automatiquement tous les produits
    List<ProductIndexDto> dtos = products.stream()
            .map(productMapper::toDto)
            .toList();
    
    model.addAttribute("products", dtos);
    return "product/index";
}
```

**Pros** :
- ✅ Code centralisé et réutilisable
- ✅ Facile à tester
- ✅ Injectables dans d'autres services

**Cons** :
- ❌ Plus de code boilerplate
- ❌ Encore du mapping manuel

---

## 💻 Implémentation dans notre projet {#implementation}

### Structure actuelle

```
src/main/java/
├── entities/
│   ├── Product.java      ← Entité JPA (BD)
│   ├── Category.java
│   └── Stock.java
│
└── models/               ← DTOs (API)
    ├── ProductFilter.java
    ├── product/
    │   ├── ProductIndexDto.java
    │   └── ProductForm.java
    └── category/
        └── CategoryDto.java
```

### Flux d'une requête GET

```
1. Navigateur : GET /product
        ↓
2. Controller : productRepository.findAll()
        ↓
3. BD : [Product, Product, Product, ...]
        ↓
4. Mapper : ProductIndexDto.fromEntity(product) × N
        ↓
5. Model : model.addAttribute("products", dtos)
        ↓
6. Template : affiche les DTOs
        ↓
7. Navigateur : voit la page HTML
```

### Flux d'une requête POST (formulaire)

```
1. Navigateur : POST /product/create + form data
        ↓
2. Spring : @ModelAttribute ProductForm (mapping auto)
        ↓
3. Validation : @NotBlank, @Min, etc. ✅
        ↓
4. Mapper : productForm.toEntity()
        ↓
5. BD : repository.save(product)
        ↓
6. Redirect : return "redirect:/product"
        ↓
7. Navigateur : GET /product (affiche la liste)
```

### Exemple d'implémentation

**Controller** :
```java
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    // 📋 Afficher la liste
    @GetMapping
    public String index(Model model) {
        List<Product> products = productRepository.findAll();
        List<ProductIndexDto> dtos = products.stream()
                .map(ProductIndexDto::fromEntity)
                .toList();
        model.addAttribute("products", dtos);
        return "product/index";
    }
    
    // ➕ Afficher le formulaire de création
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("product", new ProductForm());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product/create";
    }
    
    // 💾 Traiter le formulaire
    @PostMapping("/create")
    public String create(
            @ModelAttribute ProductForm form,
            BindingResult errors
    ) {
        if (errors.hasErrors()) {
            return "product/create";
        }
        
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow();
        
        Product product = form.toEntity();  // ✅ DTO → Entity
        product.setCategory(category);
        
        productRepository.save(product);
        
        return "redirect:/product";
    }
}
```

---

## 🚀 MapStruct : Automatiser le mapping {#mapstruct}

### Le problème du mapping manuel

Avec 30 DTOs et 30 entités, c'est beaucoup de code répétitif :

```java
public ProductDto toDto(Product p) {
    return new ProductDto(
        p.getId(),
        p.getName(),
        p.getPrice(),
        p.getDescription(),
        p.getImageUrl(),
        categoryToDto(p.getCategory()),
        stockToDto(p.getStock())
        // ... 20 autres champs
    );
}
```

**C'est ennuyeux** et **source d'erreurs**.

### La solution : MapStruct

**MapStruct** est une **library qui génère automatiquement le code de mapping** à la compilation.

C'est comme un compilateur qui regarde une interface et dit :
> "Ah, tu veux convertir `Product` en `ProductDto` ? Je vais générer le code automatiquement."

### Comment ça marche ?

#### 1️⃣ Ajouter MapStruct au pom.xml

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

#### 2️⃣ Créer l'interface Mapper

```java
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    
    // Instance singleton (pattern)
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    
    // Mapping automatique Entity → DTO
    ProductIndexDto toDto(Product product);
    
    // Mapping inverse DTO → Entity
    Product toEntity(ProductForm form);
    
    // Mapping des relations
    CategoryDto categoryToCategoryDto(Category category);
}
```

#### 3️⃣ Utiliser le Mapper

```java
@Controller
@RequestMapping("/product")
public class ProductController {
    
    private static final ProductMapper mapper = ProductMapper.INSTANCE;
    
    @GetMapping
    public String index(Model model) {
        List<Product> products = productRepository.findAll();
        
        // Mapping automatique !
        List<ProductIndexDto> dtos = products.stream()
                .map(mapper::toDto)      // ← MapStruct le fait automatiquement
                .toList();
        
        model.addAttribute("products", dtos);
        return "product/index";
    }
}
```

### Ce que MapStruct génère

À la compilation, MapStruct génère une classe `ProductMapperImpl` qui contient :

```java
// Généré automatiquement par MapStruct
public class ProductMapperImpl implements ProductMapper {
    
    @Override
    public ProductIndexDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        return new ProductIndexDto(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            categoryToCategoryDto(product.getCategory())
        );
    }
    
    @Override
    public CategoryDto categoryToCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        
        return new CategoryDto(
            category.getId(),
            category.getName()
        );
    }
}
```

### MapStruct vs mapping manuel

| Aspect | Manuel | MapStruct |
|--------|--------|-----------|
| **Temps d'écriture** | 🐌 Lent (beaucoup de code) | 🚀 Rapide (juste l'interface) |
| **Erreurs** | ❌ Facile de se tromper | ✅ Compilé, pas d'erreurs runtime |
| **Performance** | ✅ Optimal | ✅ Même perf (code généré) |
| **Maintenabilité** | 😞 Répétitif | 😊 Centralisé et lisible |
| **Courbe d'apprentissage** | ✅ Facile | 📚 Faut comprendre les annotations |

### Cas avancés avec MapStruct

#### Ignorer certains champs

```java
@Mapper
public interface ProductMapper {
    
    @Mapping(target = "id", ignore = true)  // ← Ignore ce champ
    Product toEntity(ProductForm form);
}
```

#### Renommer des champs

```java
@Mapper
public interface ProductMapper {
    
    @Mapping(source = "imageUrl", target = "imgUrl")  // ← Renomme
    ProductIndexDto toDto(Product product);
}
```

#### Custom mapping

```java
@Mapper
public interface ProductMapper {
    
    @Mapping(target = "category", expression = "java(mapCategory(product.getCategory()))")
    ProductIndexDto toDto(Product product);
    
    default CategoryDto mapCategory(Category category) {
        // Logique custom ici
        return new CategoryDto(category.getId(), category.getName().toUpperCase());
    }
}
```

---

## 📚 Résumé

### Les 3 types de DTOs

| Type | Utilité | Exemple |
|------|---------|---------|
| **Output DTO** | Envoyer au client | ProductIndexDto |
| **Input DTO** | Recevoir du client | ProductForm |
| **Query DTO** | Paramètres de filtrage | ProductFilter |

### Mapping : 3 approches

| Approche | Complexité | Automatisation |
|----------|-----------|----------------|
| **fromEntity() statique** | ⭐ Facile | ❌ Manuel |
| **Classe Mapper** | ⭐⭐ Moyen | ❌ Manuel |
| **MapStruct** | ⭐⭐⭐ Complexe | ✅ Auto |

### À retenir

1. **Séparez les données** (Entité BD ≠ DTO API)
2. **Exposez que ce qui est nécessaire** (sécurité)
3. **Utilisez les DTOs pour la validation** (avant la BD)
4. **Mappez toujours** Entité → DTO (avant d'envoyer au client)
5. **MapStruct pour gagner du temps** (si beaucoup de mappings)
