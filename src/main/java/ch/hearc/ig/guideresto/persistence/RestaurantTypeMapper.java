package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Mapper pour la classe RestaurantType
 * Gère la persistance des types de restaurants dans la table TYPES_GASTRONOMIQUES
 */
public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    // CACHE
    private static final java.util.Map<Integer, RestaurantType> cache = new java.util.HashMap<>();

    // SINGLETON
    private static RestaurantTypeMapper instance;

    private RestaurantTypeMapper() {
    }

    public static RestaurantTypeMapper getInstance() {
        if (instance == null) {
            instance = new RestaurantTypeMapper();
        }
        return instance;
    }

    // REQUÊTES SQL
    private static final String FIND_BY_ID = "SELECT numero, libelle, description FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, libelle, description FROM TYPES_GASTRONOMIQUES ORDER BY libelle";
    private static final String INSERT = "INSERT INTO TYPES_GASTRONOMIQUES (libelle, description) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE TYPES_GASTRONOMIQUES SET libelle = ?, description = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";
    private static final String SEQUENCE = "SELECT SEQ_TYPES_GASTRONOMIQUES.CURRVAL FROM DUAL";

    // CREATE (INSERT)
    @Override
    public RestaurantType create(RestaurantType type) {
        if (type == null) {
            logger.warn("Tentative de création d'un type null");
            return null;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, type.getLabel());

            if (type.getDescription() != null) {
                stmt.setString(2, type.getDescription());
            } else {
                stmt.setNull(2, Types.CLOB);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                type.setId(getSequenceValue());
                connection.commit();
                cache.put(type.getId(), type);
                logger.info("Type créé avec succès : {} (ID: {})", type.getLabel(), type.getId());
                return type;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création du type: {}", ex.getMessage());
        }
        return null;
    }

    // READ (SELECT)
    @Override
    public RestaurantType findById(int id) {
        if (cache.containsKey(id)) {
            logger.debug("Type {} trouvé dans le cache", id);
            return cache.get(id);
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RestaurantType type = mapResultSetToType(rs);
                    cache.put(id, type);
                    return type;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche du type {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RestaurantType type = mapResultSetToType(rs);
                types.add(type);
                cache.put(type.getId(), type);
            }
            logger.info("{} types de restaurants chargés", types.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des types: {}", ex.getMessage());
        }
        return types;
    }

    // UPDATE
    @Override
    public boolean update(RestaurantType type) {
        if (type == null || type.getId() == null) {
            logger.warn("Tentative de mise à jour d'un type null ou sans ID");
            return false;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, type.getLabel());

            if (type.getDescription() != null) {
                stmt.setString(2, type.getDescription());
            } else {
                stmt.setNull(2, Types.CLOB);
            }

            stmt.setInt(3, type.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                cache.put(type.getId(), type);
                logger.info("Type {} mis à jour avec succès", type.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour du type: {}", ex.getMessage());
        }
        return false;
    }

    // DELETE
    @Override
    public boolean delete(RestaurantType type) {
        return type != null && deleteById(type.getId());
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
                logger.info("Type {} supprimé avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression du type: {}", ex.getMessage());
        }
        return false;
    }

    // MÉTHODES UTILITAIRES
    private RestaurantType mapResultSetToType(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String label = rs.getString("libelle");
        String description = rs.getString("description");

        return new RestaurantType(id, label, description);
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
        logger.debug("Cache des types vidé");
    }

    @Override
    protected void addToCache(RestaurantType type) {
        if (type != null && type.getId() != null) {
            cache.put(type.getId(), type);
        }
    }

    @Override
    protected void removeFromCache(Integer id) {
        cache.remove(id);
    }
}