package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.exceptions.DatabaseOperationException;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    private static BasicEvaluationMapper instance;

    private BasicEvaluationMapper() {}

    public static BasicEvaluationMapper getInstance() {
        if (instance == null) {
            instance = new BasicEvaluationMapper();
        }
        return instance;
    }

    private static final String FIND_BY_ID = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES ORDER BY date_eval DESC";
    private static final String FIND_BY_RESTAURANT = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES WHERE fk_rest = ? ORDER BY date_eval DESC";
    private static final String INSERT = "INSERT INTO LIKES (appreciation, date_eval, adresse_ip, fk_rest) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE LIKES SET appreciation = ?, date_eval = ?, adresse_ip = ?, fk_rest = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM LIKES WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM LIKES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM LIKES";
    private static final String SEQUENCE = "SELECT SEQ_EVAL.CURRVAL FROM DUAL";

    @Override
    public BasicEvaluation create(BasicEvaluation evaluation) {
        if (evaluation == null) {
            logger.warn("Tentative de création d'une évaluation basique null");
            return null;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, evaluation.getLikeRestaurant() ? "T" : "F");
            stmt.setDate(2, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(3, evaluation.getIpAddress());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                evaluation.setId(getSequenceValue());
                addToCache(evaluation);
                logger.info("Évaluation basique créée avec succès (ID: {})", evaluation.getId());
                return evaluation;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la création de l'évaluation basique: {}", ex.getMessage());
            throw new DatabaseOperationException("CREATE BasicEvaluation", ex);
        }
        return null;
    }

    @Override
    protected BasicEvaluation findByIdFromDb(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBasicEvaluation(rs);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de l'évaluation basique {}: {}", id, ex.getMessage());
            throw new DatabaseOperationException("FIND BasicEvaluation by ID", ex);
        }
        return null;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> evaluations = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BasicEvaluation evaluation = mapResultSetToBasicEvaluation(rs);
                evaluations.add(evaluation);
                addToCache(evaluation);
            }
            logger.info("{} évaluations basiques chargées", evaluations.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des évaluations basiques: {}", ex.getMessage());
            throw new DatabaseOperationException("FIND ALL BasicEvaluations", ex);
        }
        return evaluations;
    }

    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        Set<BasicEvaluation> evaluations = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_RESTAURANT)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BasicEvaluation evaluation = mapResultSetToBasicEvaluation(rs);
                    evaluations.add(evaluation);
                    addToCache(evaluation);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche des évaluations pour le restaurant {}: {}", restaurantId, ex.getMessage());
            throw new DatabaseOperationException("FIND BasicEvaluations by Restaurant", ex);
        }
        return evaluations;
    }

    @Override
    public boolean update(BasicEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            logger.warn("Tentative de mise à jour d'une évaluation basique null ou sans ID");
            return false;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, evaluation.getLikeRestaurant() ? "T" : "F");
            stmt.setDate(2, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(3, evaluation.getIpAddress());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            stmt.setInt(5, evaluation.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                addToCache(evaluation);
                logger.info("Évaluation basique {} mise à jour avec succès", evaluation.getId());
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la mise à jour de l'évaluation basique: {}", ex.getMessage());
            throw new DatabaseOperationException("UPDATE BasicEvaluation", ex);
        }
        return false;
    }

    @Override
    public boolean delete(BasicEvaluation evaluation) {
        return evaluation != null && deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                removeFromCache(id);
                logger.info("Évaluation basique {} supprimée avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la suppression de l'évaluation basique: {}", ex.getMessage());
            throw new DatabaseOperationException("DELETE BasicEvaluation", ex);
        }
        return false;
    }

    private BasicEvaluation mapResultSetToBasicEvaluation(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String appreciation = rs.getString("appreciation");
        Date visitDate = rs.getDate("date_eval");
        String ipAddress = rs.getString("adresse_ip");
        int restaurantId = rs.getInt("fk_rest");
        Restaurant restaurant = RestaurantMapper.getInstance().findById(restaurantId);
        Boolean likeRestaurant = "T".equals(appreciation);
        return new BasicEvaluation(id, visitDate, restaurant, likeRestaurant, ipAddress);
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