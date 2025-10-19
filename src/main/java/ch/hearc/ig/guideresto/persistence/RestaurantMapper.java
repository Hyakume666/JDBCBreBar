package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private static RestaurantMapper instance;

    private RestaurantMapper() {
    }

    public static RestaurantMapper getInstance() {
        if (instance == null) {
            instance = new RestaurantMapper();
        }
        return instance;
    }

    private static final String FIND_BY_ID = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill FROM RESTAURANTS r WHERE r.numero = ?";
    private static final String FIND_ALL = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill FROM RESTAURANTS r ORDER BY r.nom";
    private static final String INSERT = "INSERT INTO RESTAURANTS (nom, adresse, description, site_web, fk_type, fk_vill) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE RESTAURANTS SET nom = ?, adresse = ?, description = ?, site_web = ?, fk_type = ?, fk_vill = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM RESTAURANTS WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM RESTAURANTS";
    private static final String SEQUENCE = "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL";

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
            throw new RuntimeException(ex);
        }
        return null;
    }

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
            throw new RuntimeException(ex);
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
                addToCache(restaurant);
            }
            logger.info("{} restaurants chargés", restaurants.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des restaurants: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        return restaurants;
    }

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
            throw new RuntimeException(ex);
        }

        return false;
    }

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
                removeFromCache(id);
                logger.info("Restaurant {} supprimé avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

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
                    addToCache(restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par nom: {}", ex.getMessage());
            throw new RuntimeException(ex);
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
                    addToCache(restaurant);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche par type: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        return restaurants;
    }


    /**
     * Convertit une ligne de ResultSet en objet Restaurant
     * Charge aussi les objets liés (City et RestaurantType)
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
    /**
     * Configure les paramètres communs pour insert et update
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

}