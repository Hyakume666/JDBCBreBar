package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.exceptions.DatabaseOperationException;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper pour la persistance des restaurants en base de données Oracle.
 * Implémente le pattern Data Mapper avec cache (Identity Map).
 *
 * <p>Ce Mapper gère la correspondance entre les objets {@link Restaurant}
 * et la table RESTAURANTS en base de données.</p>
 *
 * <p><strong>Table SQL associée :</strong></p>
 * <pre>
 * CREATE TABLE RESTAURANTS (
 *     numero      NUMBER(10)    NOT NULL PRIMARY KEY,
 *     nom         VARCHAR2(100) NOT NULL,
 *     adresse     VARCHAR2(100) NOT NULL,
 *     description CLOB,
 *     site_web    VARCHAR2(100),
 *     fk_type     NUMBER(10)    NOT NULL,
 *     fk_vill     NUMBER(10)    NOT NULL,
 *     FOREIGN KEY (fk_type) REFERENCES TYPES_GASTRONOMIQUES(numero),
 *     FOREIGN KEY (fk_vill) REFERENCES VILLES(numero)
 * );
 * </pre>
 *
 * <p><strong>Fonctionnalités :</strong></p>
 * <ul>
 *   <li>Opérations CRUD complètes sur les restaurants</li>
 *   <li>Recherche par nom (LIKE insensible à la casse)</li>
 *   <li>Recherche par type de restaurant</li>
 *   <li>Chargement automatique des relations (City, RestaurantType)</li>
 *   <li>Gestion du cache via AbstractMapper</li>
 * </ul>
 *
 * <p><strong>Relations chargées automatiquement :</strong></p>
 * <ul>
 *   <li>City (via CityMapper) - Ville du restaurant</li>
 *   <li>RestaurantType (via RestaurantTypeMapper) - Type de cuisine</li>
 * </ul>
 *
 * <p><strong>Pattern utilisé :</strong> Singleton</p>
 *
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre>
 * RestaurantMapper mapper = RestaurantMapper.getInstance();
 *
 * // Recherche par ID (avec cache)
 * Restaurant restaurant = mapper.findById(1);
 *
 * // Recherche par nom
 * Set&lt;Restaurant&gt; pizzerias = mapper.findByNameContains("pizza");
 *
 * // Création
 * Restaurant newRestaurant = new Restaurant(...);
 * Restaurant created = mapper.create(newRestaurant);
 * </pre>
 *
 * <p><strong>Gestion des transactions :</strong><br>
 * Ce Mapper n'effectue PAS de commit/rollback. La gestion transactionnelle
 * doit être assurée par la couche Service.</p>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see AbstractMapper
 * @see Restaurant
 * @see City
 * @see RestaurantType
 */
public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private static RestaurantMapper instance;

    /**
     * Constructeur privé pour le pattern Singleton.
     */
    private RestaurantMapper() {
    }

    /**
     * Retourne l'instance unique du RestaurantMapper (pattern Singleton).
     * Crée l'instance lors du premier appel (lazy initialization).
     *
     * @return L'instance unique de RestaurantMapper, jamais null
     */
    public static RestaurantMapper getInstance() {
        if (instance == null) {
            instance = new RestaurantMapper();
        }
        return instance;
    }

    // ========================================================================
    // REQUÊTES SQL
    // ========================================================================

    private static final String FIND_BY_ID = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill FROM RESTAURANTS r WHERE r.numero = ?";
    private static final String FIND_ALL = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill FROM RESTAURANTS r ORDER BY r.nom";
    private static final String INSERT = "INSERT INTO RESTAURANTS (nom, adresse, description, site_web, fk_type, fk_vill) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE RESTAURANTS SET nom = ?, adresse = ?, description = ?, site_web = ?, fk_type = ?, fk_vill = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM RESTAURANTS WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM RESTAURANTS";
    private static final String SEQUENCE = "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL";

    // ========================================================================
    // OPÉRATIONS CRUD
    // ========================================================================

    /**
     * Crée un nouveau restaurant en base de données.
     *
     * <p><strong>Pré-requis :</strong></p>
     * <ul>
     *   <li>Le restaurant ne doit pas être null</li>
     *   <li>Le nom est obligatoire</li>
     *   <li>L'adresse (rue + ville) est obligatoire</li>
     *   <li>Le type de restaurant est obligatoire et doit exister en base</li>
     *   <li>La ville doit exister en base</li>
     * </ul>
     *
     * <p><strong>Comportement :</strong></p>
     * <ol>
     *   <li>Validation de l'objet (non null)</li>
     *   <li>Exécution de l'INSERT SQL</li>
     *   <li>Récupération de l'ID généré par la séquence Oracle</li>
     *   <li>Assignation de l'ID au restaurant</li>
     *   <li>Ajout du restaurant au cache</li>
     * </ol>
     *
     * <p><strong>Gestion des valeurs nullables :</strong></p>
     * <ul>
     *   <li>description : peut être null (setNull avec Types.CLOB)</li>
     *   <li>site_web : peut être null (setNull avec Types.VARCHAR)</li>
     * </ul>
     *
     * <p><strong>⚠️ Important :</strong> Cette méthode ne fait PAS de commit.
     * Le commit doit être géré par la couche Service.</p>
     *
     * @param restaurant Le restaurant à créer (ne doit pas avoir d'ID)
     * @return Le restaurant créé avec son ID généré, ou null en cas d'échec
     * @throws DatabaseOperationException en cas d'erreur SQL
     * @see #setRestaurantParameters(PreparedStatement, Restaurant)
     */
    @Override
    public Restaurant create(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Tentative de création d'un restaurant null");
            return null;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            setRestaurantParameters(stmt, restaurant);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                restaurant.setId(getSequenceValue());
                addToCache(restaurant);
                logger.info("Restaurant créé avec succès : {} (ID: {})", restaurant.getName(), restaurant.getId());
                return restaurant;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la création du restaurant: {}", ex.getMessage());
            throw new DatabaseOperationException("CREATE Restaurant", ex);
        }
        return null;
    }

    /**
     * Recherche un restaurant par son ID directement en base de données.
     *
     * <p>Cette méthode est appelée par {@link AbstractMapper#findById(int)}
     * uniquement si le restaurant n'est pas dans le cache.</p>
     *
     * <p><strong>Relations chargées :</strong></p>
     * <ul>
     *   <li>City (ville du restaurant) via CityMapper</li>
     *   <li>RestaurantType (type de cuisine) via RestaurantTypeMapper</li>
     * </ul>
     *
     * <p><strong>Note :</strong> Les évaluations ne sont PAS chargées par cette méthode.
     * Utiliser {@link PersistenceHelper} pour charger un restaurant avec ses évaluations.</p>
     *
     * @param id L'identifiant unique du restaurant
     * @return Le restaurant trouvé en base, ou null si inexistant
     * @throws DatabaseOperationException en cas d'erreur SQL
     * @see #mapResultSetToRestaurant(ResultSet)
     */
    @Override
    protected Restaurant findByIdFromDb(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRestaurant(rs);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche du restaurant {}: {}", id, ex.getMessage());
            throw new DatabaseOperationException("FIND Restaurant by ID", ex);
        }
        return null;
    }

    /**
     * Récupère tous les restaurants depuis la base de données.
     *
     * <p>Les restaurants sont triés par ordre alphabétique du nom.</p>
     *
     * <p><strong>Relations chargées :</strong></p>
     * <ul>
     *   <li>City (ville du restaurant)</li>
     *   <li>RestaurantType (type de cuisine)</li>
     * </ul>
     *
     * <p><strong>Note :</strong> Les évaluations ne sont PAS chargées.
     * Utiliser {@link PersistenceHelper#loadAllRestaurantsWithEvaluations()}
     * pour charger les restaurants avec leurs évaluations.</p>
     *
     * <p><strong>⚠️ Performance :</strong> Sur de grandes bases, cette méthode
     * peut être coûteuse. Envisager une pagination si nécessaire.</p>
     *
     * @return Un Set de tous les restaurants. Jamais null, peut être vide.
     * @throws DatabaseOperationException en cas d'erreur SQL
     */
    @Override
    public Set<Restaurant> findAll() {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Restaurant restaurant = mapResultSetToRestaurant(rs);
                restaurants.add(restaurant);
                addToCache(restaurant);
            }
            logger.info("{} restaurants chargés", restaurants.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des restaurants: {}", ex.getMessage());
            throw new DatabaseOperationException("FIND ALL Restaurants", ex);
        }
        return restaurants;
    }

    /**
     * Met à jour un restaurant existant en base de données.
     *
     * <p><strong>Pré-requis :</strong></p>
     * <ul>
     *   <li>Le restaurant ne doit pas être null</li>
     *   <li>Le restaurant doit avoir un ID valide (non null)</li>
     *   <li>Le restaurant doit exister en base</li>
     * </ul>
     *
     * <p><strong>Comportement :</strong></p>
     * <ol>
     *   <li>Validation de l'objet</li>
     *   <li>Exécution de l'UPDATE SQL</li>
     *   <li>Si succès : mise à jour du cache</li>
     * </ol>
     *
     * <p><strong>Champs mis à jour :</strong></p>
     * <ul>
     *   <li>Nom</li>
     *   <li>Adresse (rue)</li>
     *   <li>Description</li>
     *   <li>Site web</li>
     *   <li>Type de restaurant (fk_type)</li>
     *   <li>Ville (fk_vill)</li>
     * </ul>
     *
     * <p><strong>⚠️ Important :</strong> Cette méthode ne fait PAS de commit.</p>
     *
     * @param restaurant Le restaurant à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi (au moins 1 ligne modifiée), false sinon
     * @throws DatabaseOperationException en cas d'erreur SQL
     */
    @Override
    public boolean update(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative de mise à jour d'un restaurant null ou sans ID");
            return false;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            setRestaurantParameters(stmt, restaurant);
            stmt.setInt(7, restaurant.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                addToCache(restaurant);
                logger.info("Restaurant {} mis à jour avec succès", restaurant.getId());
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la mise à jour du restaurant: {}", ex.getMessage());
            throw new DatabaseOperationException("UPDATE Restaurant", ex);
        }

        return false;
    }

    /**
     * Supprime un restaurant de la base de données.
     * Délègue à {@link #deleteById(int)}.
     *
     * <p><strong>⚠️ Attention :</strong> Cette méthode ne supprime PAS les évaluations
     * associées. Pour une suppression complète, utiliser
     * {@link PersistenceHelper#deleteRestaurantCompletely(Restaurant)}.</p>
     *
     * @param restaurant Le restaurant à supprimer (doit avoir un ID valide)
     * @return true si la suppression a réussi, false sinon
     * @see #deleteById(int)
     */
    @Override
    public boolean delete(Restaurant restaurant) {
        return restaurant != null && deleteById(restaurant.getId());
    }

    /**
     * Supprime un restaurant par son ID.
     *
     * <p><strong>Comportement :</strong></p>
     * <ol>
     *   <li>Exécution du DELETE SQL</li>
     *   <li>Si succès : retrait du restaurant du cache</li>
     * </ol>
     *
     * <p><strong>⚠️ Attention :</strong> Cette méthode échouera si le restaurant
     * a des évaluations (contrainte d'intégrité référentielle). Supprimer d'abord
     * les évaluations ou utiliser
     * {@link PersistenceHelper#deleteRestaurantCompletely(Restaurant)}.</p>
     *
     * <p><strong>⚠️ Important :</strong> Cette méthode ne fait PAS de commit.</p>
     *
     * @param id L'identifiant du restaurant à supprimer
     * @return true si la suppression a réussi (au moins 1 ligne supprimée), false sinon
     * @throws DatabaseOperationException en cas d'erreur SQL
     */
    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                removeFromCache(id);
                logger.info("Restaurant {} supprimé avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la suppression du restaurant: {}", ex.getMessage());
            throw new DatabaseOperationException("DELETE Restaurant", ex);
        }
        return false;
    }

    // ========================================================================
    // RECHERCHES PERSONNALISÉES
    // ========================================================================

    /**
     * Recherche des restaurants dont le nom contient la chaîne spécifiée.
     * La recherche est insensible à la casse.
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *   <li>findByNameContains("pizza") → "Pizzeria Roma", "Pizza Hut"</li>
     *   <li>findByNameContains("FLEUR") → "Fleur-de-Lys"</li>
     * </ul>
     *
     * <p><strong>SQL utilisé :</strong></p>
     * <pre>
     * SELECT ... FROM RESTAURANTS r
     * WHERE UPPER(r.nom) LIKE ?
     * ORDER BY r.nom
     * </pre>
     *
     * <p>Les résultats sont triés par ordre alphabétique.</p>
     *
     * @param name Le nom partiel à rechercher (ne peut pas être null)
     * @return Un Set des restaurants correspondants. Jamais null, peut être vide.
     * @throws DatabaseOperationException en cas d'erreur SQL
     */
    public Set<Restaurant> findByNameContains(String name) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        String query = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r WHERE UPPER(r.nom) LIKE ? ORDER BY r.nom";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name.toUpperCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    restaurants.add(restaurant);
                    addToCache(restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par nom: {}", ex.getMessage());
            throw new DatabaseOperationException("SEARCH Restaurant by name", ex);
        }
        return restaurants;
    }

    /**
     * Recherche des restaurants par type de cuisine.
     *
     * <p><strong>Exemples de types :</strong> Cuisine suisse, Pizzeria,
     * Restaurant gastronomique, etc.</p>
     *
     * <p>Les résultats sont triés par ordre alphabétique du nom.</p>
     *
     * @param type Le type de restaurant recherché (ne peut pas être null, doit avoir un ID)
     * @return Un Set des restaurants de ce type. Jamais null, peut être vide.
     * @see #findByTypeId(int)
     */
    public Set<Restaurant> findByType(RestaurantType type) {
        if (type == null || type.getId() == null) {
            return new HashSet<>();
        }
        return findByTypeId(type.getId());
    }

    /**
     * Recherche des restaurants par ID de type de cuisine.
     *
     * <p><strong>SQL utilisé :</strong></p>
     * <pre>
     * SELECT ... FROM RESTAURANTS r
     * WHERE r.fk_type = ?
     * ORDER BY r.nom
     * </pre>
     *
     * @param typeId L'identifiant du type de restaurant
     * @return Un Set des restaurants de ce type. Jamais null, peut être vide.
     * @throws DatabaseOperationException en cas d'erreur SQL
     */
    public Set<Restaurant> findByTypeId(int typeId) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        String query = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r WHERE r.fk_type = ? ORDER BY r.nom";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, typeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    restaurants.add(restaurant);
                    addToCache(restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par type: {}", ex.getMessage());
            throw new DatabaseOperationException("SEARCH Restaurant by type", ex);
        }
        return restaurants;
    }

    // ========================================================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================================================

    /**
     * Convertit une ligne de ResultSet en objet Restaurant.
     *
     * <p><strong>Chargement des relations :</strong></p>
     * <ul>
     *   <li>La ville est chargée via {@link CityMapper#findById(int)}</li>
     *   <li>Le type est chargé via {@link RestaurantTypeMapper#findById(int)}</li>
     * </ul>
     *
     * <p><strong>Colonnes attendues dans le ResultSet :</strong></p>
     * <ul>
     *   <li>numero (int) - ID du restaurant</li>
     *   <li>nom (String) - Nom du restaurant</li>
     *   <li>adresse (String) - Rue</li>
     *   <li>description (String) - Description (nullable)</li>
     *   <li>site_web (String) - URL du site (nullable)</li>
     *   <li>fk_type (int) - ID du type de restaurant</li>
     *   <li>fk_vill (int) - ID de la ville</li>
     * </ul>
     *
     * @param rs Le ResultSet positionné sur la ligne à convertir
     * @return Le restaurant construit depuis le ResultSet
     * @throws SQLException en cas d'erreur lors de la lecture du ResultSet
     */
    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        int id = rs.getInt("numero");
        String name = rs.getString("nom");
        String street = rs.getString("adresse");
        String description = rs.getString("description");
        String website = rs.getString("site_web");
        int typeId = rs.getInt("fk_type");
        int cityId = rs.getInt("fk_vill");

        City city = CityMapper.getInstance().findById(cityId);
        RestaurantType type = RestaurantTypeMapper.getInstance().findById(typeId);
        Localisation localisation = new Localisation(street, city);

        return new Restaurant(id, name, description, website, localisation, type);
    }

    /**
     * Configure les paramètres communs pour les requêtes INSERT et UPDATE.
     *
     * <p><strong>Paramètres configurés (dans l'ordre) :</strong></p>
     * <ol>
     *   <li>nom (String) - Obligatoire</li>
     *   <li>adresse (String) - Obligatoire</li>
     *   <li>description (String ou NULL) - Optionnel</li>
     *   <li>site_web (String ou NULL) - Optionnel</li>
     *   <li>fk_type (int) - Obligatoire</li>
     *   <li>fk_vill (int) - Obligatoire</li>
     * </ol>
     *
     * <p><strong>Gestion des valeurs nullables :</strong><br>
     * Pour les champs optionnels (description, site_web), la méthode utilise
     * setNull() avec le type SQL approprié si la valeur est null.</p>
     *
     * @param stmt Le PreparedStatement à configurer
     * @param restaurant Le restaurant contenant les valeurs
     * @throws SQLException en cas d'erreur lors de la configuration des paramètres
     */
    private void setRestaurantParameters(PreparedStatement stmt, Restaurant restaurant) throws SQLException {
        stmt.setString(1, restaurant.getName());
        stmt.setString(2, restaurant.getAddress().getStreet());

        if (restaurant.getDescription() != null) {
            stmt.setString(3, restaurant.getDescription());
        } else {
            stmt.setNull(3, Types.CLOB);
        }

        if (restaurant.getWebsite() != null) {
            stmt.setString(4, restaurant.getWebsite());
        } else {
            stmt.setNull(4, Types.VARCHAR);
        }

        stmt.setInt(5, restaurant.getType().getId());
        stmt.setInt(6, restaurant.getAddress().getCity().getId());
    }

    // ========================================================================
    // IMPLÉMENTATION DES MÉTHODES ABSTRAITES
    // ========================================================================

    /**
     * Retourne la requête SQL pour récupérer la valeur actuelle de la séquence.
     *
     * @return "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL"
     */
    @Override
    protected String getSequenceQuery() {
        return SEQUENCE;
    }

    /**
     * Retourne la requête SQL pour vérifier l'existence d'un restaurant.
     *
     * @return "SELECT 1 FROM RESTAURANTS WHERE numero = ?"
     */
    @Override
    protected String getExistsQuery() {
        return EXISTS;
    }

    /**
     * Retourne la requête SQL pour compter le nombre total de restaurants.
     *
     * @return "SELECT COUNT(*) FROM RESTAURANTS"
     */
    @Override
    protected String getCountQuery() {
        return COUNT;
    }
}