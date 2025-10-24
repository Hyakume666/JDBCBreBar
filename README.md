# GuideResto - Persistance JDBC

Application Java démontrant l'implémentation de la **persistance de données** avec JDBC et les patterns de conception classiques.

## Objectif

Maîtriser la persistance objet-relationnelle en Java :
- Mapping objet-relationnel manuel (sans ORM)
- Patterns de persistance professionnels
- Gestion des transactions ACID
- Optimisation des performances

## Stack technique

- **Java 21** + **JDBC**
- **Oracle Database** (Sequences + Triggers)
- **Maven** pour les dépendances
- **Log4j2** pour la traçabilité

## Architecture
```
Business Layer (Entités)
         ↓
Service Layer (Logique métier + Transactions)
         ↓
Persistence Layer (Mappers + Cache)
         ↓
Oracle Database
```

### Composants clés

- **AbstractMapper\<T\>** : Classe générique avec cache (Identity Map)
- **Mappers concrets** : RestaurantMapper, CityMapper, EvaluationMapper...
- **ConnectionUtils** : Gestion de la connexion unique
- **PersistenceHelper** : Opérations complexes multi-mappers

## Patterns implémentés

### 1. Data Mapper
Séparation totale entre objets métier et logique SQL.
```java
Restaurant r = RestaurantMapper.getInstance().findById(1);
```

### 2. Identity Map (Cache)
Évite les requêtes redondantes.
```java
// 1ère fois : requête SQL
Restaurant r1 = RestaurantMapper.getInstance().findById(1);

// 2ème fois : cache hit
Restaurant r2 = RestaurantMapper.getInstance().findById(1);
// r1 == r2 → true
```

### 3. Unit of Work
Gestion transactionnelle complète.
```java
try {
    Restaurant created = RestaurantMapper.getInstance().create(restaurant);
    connection.commit();  // Succès
} catch (Exception ex) {
    connection.rollback();  // Annulation
}
```

### 4. Singleton
Une instance par Mapper pour partager le cache.
```java
RestaurantMapper.getInstance()
```

### 5. Lazy Loading
Chargement des relations à la demande.
```java
// Sans relations
Restaurant r = RestaurantMapper.getInstance().findById(1);

// Avec relations
Restaurant r = PersistenceHelper.loadRestaurantWithEvaluations(1);
```

## Modèle de données
```
RESTAURANTS (1) ──→ (N) COMMENTAIRES (CompleteEvaluation)
                         └─→ (N) NOTES (Grade)
                                  └─→ (1) CRITERES_EVALUATION

RESTAURANTS (1) ──→ (N) LIKES (BasicEvaluation)

RESTAURANTS (N) ──→ (1) VILLES
RESTAURANTS (N) ──→ (1) TYPES_GASTRONOMIQUES
```

## Installation

### 1. Créer la base de données
```bash
sqlplus user/password@db @GuideResto_CREATE_TABLES.sql
```

### 2. Configurer la connexion

Créer `src/main/resources/database.properties` :
```properties
database.url=jdbc:oracle:thin:@localhost:1521:XE
database.username=votre_user
database.password=votre_password
```

### 3. Compiler et lancer
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="ch.hearc.ig.guideresto.presentation.Application"
```

## Structure
```
persistence/
├── AbstractMapper.java              # Générique + Cache
├── ConnectionUtils.java             # Connexion unique
├── PersistenceHelper.java           # Opérations complexes
├── RestaurantMapper.java
├── CityMapper.java
├── RestaurantTypeMapper.java
├── BasicEvaluationMapper.java
├── CompleteEvaluationMapper.java
├── GradeMapper.java
└── EvaluationCriteriaMapper.java
```

## Concepts démontrés

**Data Mapper Pattern** - Séparation objet/SQL  
**Identity Map** - Cache par entité  
**Unit of Work** - Transactions ACID  
**Lazy Loading** - Optimisation chargement  
**PreparedStatement** - Sécurité SQL  
**Singleton** - Instance unique