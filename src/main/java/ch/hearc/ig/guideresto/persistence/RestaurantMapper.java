package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Mapper pour la classe Restaurant
 * Gère la persistance des restaurants dans la table RESTAURANTS
 */
public class RestaurantMapper extends AbstractMapper<Restaurant> {

    // CACHE
    private static final java.util.Map<Integer, Restaurant> cache = new java.util.HashMap<>();

    // SINGLETON
    private static RestaurantMapper instance;

    private RestaurantMapper() {
    }

    public static RestaurantMapper getInstance() {
        if (instance == null) {
            instance = new RestaurantMapper();
        }
        return instance;
    }

    // === REQUÊTES SQL ===
    private static final String FIND_BY_ID =
            "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                    "FROM RESTAURANTS r WHERE r.numero = ?";

    private static final String FIND_ALL =
            "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                    "FROM RESTAURANTS r ORDER BY r.nom";

    private static final String INSERT =
            "INSERT INTO RESTAURANTS (nom, adresse, description, site_web, fk_type, fk_vill) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE RESTAURANTS SET nom = ?, adresse = ?, description = ?, site_web = ?, fk_type = ?, fk_vill = ? " +
                    "WHERE numero = ?";

    private static final String DELETE = "DELETE FROM RESTAURANTS WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM RESTAURANTS";
    private static final String SEQUENCE = "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL";

    // CREATE (INSERT)
    @Override
    public Restaurant create(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Tentative de création d'un restaurant null");
            return null;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
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

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                restaurant.setId(getSequenceValue());
                connection.commit();
                cache.put(restaurant.getId(), restaurant);
                logger.info("Restaurant créé avec succès : {} (ID: {})", restaurant.getName(), restaurant.getId());
                return restaurant;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création du restaurant: {}", ex.getMessage());
        }
        return null;
    }

    // READ (SELECT)
    @Override
    public Restaurant findById(int id) {
        if (cache.containsKey(id)) {
            logger.debug("Restaurant {} trouvé dans le cache", id);
            return cache.get(id);
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    cache.put(id, restaurant);
                    return restaurant;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche du restaurant {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<Restaurant> findAll() {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Restaurant restaurant = mapResultSetToRestaurant(rs);
                restaurants.add(restaurant);
                cache.put(restaurant.getId(), restaurant);
            }
            logger.info("{} restaurants chargés", restaurants.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des restaurants: {}", ex.getMessage());
        }
        return restaurants;
    }

    // UPDATE
    @Override
    public boolean update(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative de mise à jour d'un restaurant null ou sans ID");
            return false;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
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
            stmt.setInt(7, restaurant.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                cache.put(restaurant.getId(), restaurant);
                logger.info("Restaurant {} mis à jour avec succès", restaurant.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour du restaurant: {}", ex.getMessage());
        }
        return false;
    }

    // DELETE
    @Override
    public boolean delete(Restaurant restaurant) {
        return restaurant != null && deleteById(restaurant.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                cache.remove(id);
                logger.info("Restaurant {} supprimé avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression du restaurant: {}", ex.getMessage());
        }
        return false;
    }

    // MÉTHODES DE RECHERCHE PERSONNALISÉES

    /**
     * Recherche les restaurants par nom (contient)
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
                    cache.put(restaurant.getId(), restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par nom: {}", ex.getMessage());
        }
        return restaurants;
    }

    /**
     * Recherche les restaurants par ville
     */
    public Set<Restaurant> findByCity(City city) {
        if (city == null || city.getId() == null) {
            return new HashSet<>();
        }
        return findByCityId(city.getId());
    }

    /**
     * Recherche les restaurants par ID de ville
     */
    public Set<Restaurant> findByCityId(int cityId) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        String query = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r WHERE r.fk_vill = ? ORDER BY r.nom";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    restaurants.add(restaurant);
                    cache.put(restaurant.getId(), restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par ville: {}", ex.getMessage());
        }
        return restaurants;
    }

    /**
     * Recherche les restaurants par type
     */
    public Set<Restaurant> findByType(RestaurantType type) {
        if (type == null || type.getId() == null) {
            return new HashSet<>();
        }
        return findByTypeId(type.getId());
    }

    /**
     * Recherche les restaurants par ID de type
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
                    cache.put(restaurant.getId(), restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par type: {}", ex.getMessage());
        }
        return restaurants;
    }

    // MÉTHODES UTILITAIRES

    /**
     * Convertit une ligne de ResultSet en objet Restaurant
     * IMPORTANT : Charge aussi les objets liés (City et RestaurantType)
     */
    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String name = rs.getString("nom");
        String street = rs.getString("adresse");
        String description = rs.getString("description");
        String website = rs.getString("site_web");
        Integer typeId = rs.getInt("fk_type");
        Integer cityId = rs.getInt("fk_vill");

        City city = CityMapper.getInstance().findById(cityId);
        RestaurantType type = RestaurantTypeMapper.getInstance().findById(typeId);
        Localisation localisation = new Localisation(street, city);

        Restaurant restaurant = new Restaurant(id, name, description, website, localisation, type);

        return restaurant;
    }

    // MÉTHODES ABSTRAITES
    @Override
    protected String getSequenceQuery() {
        return SEQUENCE;
    }

    @Override
    protected String getExistsQuery() {
        return EXISTS;
    }

    @Override
    protected String getCountQuery() {
        return COUNT;
    }

    // === GESTION DU CACHE ===
    @Override
    protected boolean isCacheEmpty() {
        return cache.isEmpty();
    }

    @Override
    protected void resetCache() {
        cache.clear();
        logger.debug("Cache des restaurants vidé");
    }

    @Override
    protected void addToCache(Restaurant restaurant) {
        if (restaurant != null && restaurant.getId() != null) {
            cache.put(restaurant.getId(), restaurant);
        }
    }

    @Override
    protected void removeFromCache(Integer id) {
        cache.remove(id);
    }
}