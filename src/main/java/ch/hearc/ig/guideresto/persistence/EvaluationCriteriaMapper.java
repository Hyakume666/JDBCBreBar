package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    private static EvaluationCriteriaMapper instance;

    private EvaluationCriteriaMapper() {}

    public static EvaluationCriteriaMapper getInstance() {
        if (instance == null) {
            instance = new EvaluationCriteriaMapper();
        }
        return instance;
    }

    private static final String FIND_BY_ID = "SELECT numero, nom, description FROM CRITERES_EVALUATION WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, nom, description FROM CRITERES_EVALUATION ORDER BY nom";
    private static final String INSERT = "INSERT INTO CRITERES_EVALUATION (nom, description) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE CRITERES_EVALUATION SET nom = ?, description = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM CRITERES_EVALUATION WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM CRITERES_EVALUATION WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM CRITERES_EVALUATION";
    private static final String SEQUENCE = "SELECT SEQ_CRITERES_EVALUATION.CURRVAL FROM DUAL";

    @Override
    public EvaluationCriteria create(EvaluationCriteria criteria) {
        if (criteria == null) {
            logger.warn("Tentative de création d'un critère null");
            return null;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, criteria.getName());
            if (criteria.getDescription() != null) {
                stmt.setString(2, criteria.getDescription());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                criteria.setId(getSequenceValue());
                connection.commit();
                addToCache(criteria);
                logger.info("Critère créé avec succès : {} (ID: {})", criteria.getName(), criteria.getId());
                return criteria;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback dans create(): {}", e.getMessage());
            }
            logger.error("Erreur lors de la création du critère: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    protected EvaluationCriteria findByIdFromDb(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCriteria(rs);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche du critère {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<EvaluationCriteria> findAll() {
        Set<EvaluationCriteria> criterias = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                EvaluationCriteria criteria = mapResultSetToCriteria(rs);
                criterias.add(criteria);
                addToCache(criteria);
            }
            logger.info("{} critères d'évaluation chargés", criterias.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des critères: {}", ex.getMessage());
        }
        return criterias;
    }

    @Override
    public boolean update(EvaluationCriteria criteria) {
        if (criteria == null || criteria.getId() == null) {
            logger.warn("Tentative de mise à jour d'un critère null ou sans ID");
            return false;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, criteria.getName());
            if (criteria.getDescription() != null) {
                stmt.setString(2, criteria.getDescription());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            stmt.setInt(3, criteria.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                addToCache(criteria);
                logger.info("Critère {} mis à jour avec succès", criteria.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback dans update(): {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour du critère: {}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(EvaluationCriteria criteria) {
        return criteria != null && deleteById(criteria.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                removeFromCache(id);
                logger.info("Critère {} supprimé avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback dans deleteById(): {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression du critère: {}", ex.getMessage());
        }
        return false;
    }

    private EvaluationCriteria mapResultSetToCriteria(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String name = rs.getString("nom");
        String description = rs.getString("description");
        return new EvaluationCriteria(id, name, description);
    }

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
}