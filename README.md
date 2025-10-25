# Guide Resto

Application de gestion et d'évaluation de restaurants développée en Java avec Oracle Database.

## Description

Système permettant de :
- Gérer des restaurants (CRUD complet)
- Évaluer les restaurants (votes simples + avis détaillés)
- Rechercher des restaurants (par nom, ville, type de cuisine)
- Consulter les statistiques et moyennes des évaluations

## Architecture

### Architecture 3 couches
```
┌─────────────────────────────────────┐
│   Présentation (Application.java)   │  ← Interface console
├─────────────────────────────────────┤
│   Service (Business Logic)          │  ← RestaurantService, EvaluationService
│   - Gestion des transactions        │
│   - Validation métier                │
├─────────────────────────────────────┤
│   Persistance (Data Access)         │  ← Mappers (AbstractMapper + 8 implémentations)
│   - Mappers avec cache Identity Map │
│   - Gestion connexion Oracle        │
└─────────────────────────────────────┘
        ↓
┌─────────────────────────────────────┐
│   Oracle Database                   │
└─────────────────────────────────────┘
```

### Packages
```
ch.hearc.ig.guideresto
├── business/              # Entités métier
│   ├── Restaurant, City, RestaurantType
│   ├── Evaluation (abstract), BasicEvaluation, CompleteEvaluation
│   ├── Grade, EvaluationCriteria, Localisation
│   ├── Constants.java (3 domaines : Evaluation, UI, Messages)
│   └── IBusinessObject.java
├── service/               # Logique métier
│   ├── RestaurantService.java
│   └── EvaluationService.java
├── persistence/           # Accès aux données
│   ├── AbstractMapper.java (générique avec Identity Map)
│   ├── RestaurantMapper, CityMapper, RestaurantTypeMapper
│   ├── BasicEvaluationMapper, CompleteEvaluationMapper
│   ├── GradeMapper, EvaluationCriteriaMapper
│   ├── PersistenceHelper.java (Facade)
│   ├── ConnectionUtils.java (Singleton)
│   └── exceptions/
│       ├── PersistenceException.java
│       ├── DatabaseOperationException.java
│       └── EntityNotFoundException.java
└── presentation/          # Interface console
    └── Application.java
```

## Fonctionnalités principales

### Gestion des restaurants
- Créer, modifier, supprimer des restaurants
- Recherche par nom, ville, type de cuisine
- Affichage avec évaluations et statistiques

### Système d'évaluation double
- **Évaluation basique** : Vote simple (like/dislike) avec IP
- **Évaluation complète** : Commentaire + notes détaillées par critères (service, cuisine, cadre)

### Recherche et filtrage
- Par nom de restaurant (recherche partielle)
- Par ville
- Par type de cuisine

## Technologies

- **Langage** : Java
- **Base de données** : Oracle Database 19c
- **Build** : Maven
- **Logging** : Log4j2
- **JDBC** : ojdbc11

## Bonnes pratiques appliquées

### Principe DRY (Don't Repeat Yourself)
- **Centralisation des constantes** : Tous les messages utilisateur, éléments UI et règles métier sont regroupés dans `Constants.java`
- **Organisation par domaine** : Les constantes sont structurées en 3 classes internes :
    - `Evaluation` : Règles métier (MIN_GRADE, MAX_GRADE, IP_UNAVAILABLE)
    - `UI` : Éléments visuels (séparateurs, emojis Unicode)
    - `Messages` : Tous les messages utilisateur (erreurs, succès, infos, prompts)

### Gestion des ressources
- **Try-finally** : Fermeture garantie du Scanner et de la connexion DB
- **Logging** : Traçabilité de la fermeture des ressources
- **Prévention des fuites** : Pas de fuite mémoire ou de ressources système

### Documentation
- **JavaDoc complète** : Toutes les couches Service et Persistance documentées
- **Standards Oracle** : Respect des conventions JavaDoc officielles
- **Exemples d'utilisation** : Documentation pratique pour les développeurs

### Schéma Oracle

Le projet utilise les tables suivantes :
- `RESTAURANTS` - Données des restaurants
- `VILLES` - Liste des villes
- `TYPES_GASTRONOMIQUES` - Types de cuisine
- `LIKES` - Évaluations basiques (like/dislike)
- `COMMENTAIRES` - Évaluations complètes
- `NOTES` - Notes détaillées par critère
- `CRITERES_EVALUATIONS` - Critères d'évaluation

## Patterns de conception utilisés

- **Singleton** : Services, Mappers, ConnectionUtils
- **Data Mapper** : AbstractMapper et implémentations concrètes
- **Identity Map** : Cache dans AbstractMapper pour éviter requêtes redondantes
- **Facade** : PersistenceHelper simplifie opérations complexes
- **Template Method** : Hiérarchie Evaluation (BasicEvaluation, CompleteEvaluation)
- **Exception Hierarchy** : Gestion d'erreurs structurée

## Structure de la base de données

### Relations principales
```
RESTAURANTS (1) ──< (N) LIKES
            (1) ──< (N) COMMENTAIRES
                        (1) ──< (N) NOTES

VILLES (1) ──< (N) RESTAURANTS
TYPES_GASTRONOMIQUES (1) ──< (N) RESTAURANTS
CRITERES_EVALUATIONS (1) ──< (N) NOTES
```

## Auteurs

- **Code de base** : Cédric Baudet, Alain Matile, Arnaud Geiser
- **Améliorations (Architecture 3 couches, Services, JavaDoc)** : Jérémie Bressoud & Loïc Barthoulot

---

**Version** : 2.0  
**Date de mise à jour** : 25 Octobre 2025
```