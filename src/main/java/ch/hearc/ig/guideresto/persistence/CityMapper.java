package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Mapper pour la classe City
 * Gère la persistance des villes dans la table VILLES
 */
public class CityMapper extends AbstractMapper<City> {

    // SINGLETON
    private static CityMapper instance;

    private CityMapper() {
    }

    public static CityMapper getInstance() {
        if (instance == null) {
            instance = new CityMapper();
        }
        return instance;
    }

    // REQUÊTES SQL
    private static final String FIND_BY_ID = "SELECT numero, code_postal, nom_ville FROM VILLES WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, code_postal, nom_ville FROM VILLES ORDER BY nom_ville";
    private static final String INSERT = "INSERT INTO VILLES (code_postal, nom_ville) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE VILLES SET code_postal = ?, nom_ville = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM VILLES WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM VILLES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM VILLES";
    private static final String SEQUENCE = "SELECT SEQ_VILLES.CURRVAL FROM DUAL";

    // CREATE (INSERT)
    @Override
    public City create(City city) {
        if (city == null) {
            logger.warn("Tentative de création d'une ville null");
            return null;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, city.getZipCode());
            stmt.setString(2, city.getCityName());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré par le trigger
                city.setId(getSequenceValue());
                connection.commit();

                // Ajouter au cache
                addToCache(city);

                logger.info("Ville créée avec succès : {} (ID: {})", city.getCityName(), city.getId());
                return city;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création de la ville: {}", ex.getMessage());
        }
        return null;
    }

    // READ (SELECT)
    @Override
    protected City findByIdFromDb(int id) {
        // Le corps de la méthode ne change pas, sauf pour la gestion du cache.
        // Vous pouvez supprimer les lignes qui interagissent avec le cache ici,
        // car la classe AbstractMapper s'en occupe déjà.
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // La logique de mapping reste la même
                    return mapResultSetToCity(rs);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de la ville {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                City city = mapResultSetToCity(rs);
                cities.add(city);
                // Ajouter au cache
                addToCache(city);
            }
            logger.info("{} villes chargées depuis la base", cities.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des villes: {}", ex.getMessage());
        }
        return cities;
    }

    // UPDATE
    @Override
    public boolean update(City city) {
        if (city == null || city.getId() == null) {
            logger.warn("Tentative de mise à jour d'une ville null ou sans ID");
            return false;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, city.getZipCode());
            stmt.setString(2, city.getCityName());
            stmt.setInt(3, city.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                // Mettre à jour le cache
                addToCache(city);
                logger.info("Ville {} mise à jour avec succès", city.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour de la ville: {}", ex.getMessage());
        }
        return false;
    }

    // DELETE
    @Override
    public boolean delete(City city) {
        return city != null && deleteById(city.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                // Retirer du cache
                removeFromCache(id);
                logger.info("Ville {} supprimée avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression de la ville: {}", ex.getMessage());
        }
        return false;
    }

    // MÉTHODES UTILITAIRES

    /**
     * Convertit une ligne de ResultSet en objet City
     */
    private City mapResultSetToCity(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String zipCode = rs.getString("code_postal");
        String cityName = rs.getString("nom_ville");

        return new City(id, zipCode, cityName);
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

    @Override
    protected void addToCache(City city) {
        if (city != null && city.getId() != null) {
            addToCache(city);
        }
    }

    @Override
    protected void removeFromCache(Integer id) {
        removeFromCache(id);
    }
}