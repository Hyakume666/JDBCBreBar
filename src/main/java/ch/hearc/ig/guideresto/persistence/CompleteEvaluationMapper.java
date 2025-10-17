package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    private static CompleteEvaluationMapper instance;

    private CompleteEvaluationMapper() {}

    public static CompleteEvaluationMapper getInstance() {
        if (instance == null) {
            instance = new CompleteEvaluationMapper();
        }
        return instance;
    }

    private static final String FIND_BY_ID = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES ORDER BY date_eval DESC";
    private static final String FIND_BY_RESTAURANT = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE fk_rest = ? ORDER BY date_eval DESC";
    private static final String INSERT = "INSERT INTO COMMENTAIRES (date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE COMMENTAIRES SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM COMMENTAIRES WHERE numero = ?";
    private static final String EXISTS = "SELECT 1 FROM COMMENTAIRES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM COMMENTAIRES";
    private static final String SEQUENCE = "SELECT SEQ_EVAL.CURRVAL FROM DUAL";

    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        if (evaluation == null) {
            logger.warn("Tentative de création d'une évaluation complète null");
            return null;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            if (evaluation.getComment() != null) {
                stmt.setString(2, evaluation.getComment());
            } else {
                stmt.setNull(2, Types.CLOB);
            }
            stmt.setString(3, evaluation.getUsername());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                evaluation.setId(getSequenceValue());
                connection.commit();
                if (evaluation.getGrades() != null && !evaluation.getGrades().isEmpty()) {
                    for (Grade grade : evaluation.getGrades()) {
                        grade.setEvaluation(evaluation);
                        GradeMapper.getInstance().create(grade);
                    }
                }
                addToCache(evaluation);
                logger.info("Évaluation complète créée avec succès (ID: {})", evaluation.getId());
                return evaluation;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création de l'évaluation complète: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    protected CompleteEvaluation findByIdFromDb(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompleteEvaluation(rs);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de l'évaluation complète {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        Set<CompleteEvaluation> evaluations = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CompleteEvaluation evaluation = mapResultSetToCompleteEvaluation(rs);
                evaluations.add(evaluation);
                addToCache(evaluation);
            }
            logger.info("{} évaluations complètes chargées", evaluations.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des évaluations complètes: {}", ex.getMessage());
        }
        return evaluations;
    }

    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        Set<CompleteEvaluation> evaluations = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_RESTAURANT)) {
            stmt.setInt(1, restaurantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CompleteEvaluation evaluation = mapResultSetToCompleteEvaluation(rs);
                    evaluations.add(evaluation);
                    addToCache(evaluation);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche des évaluations pour le restaurant {}: {}", restaurantId, ex.getMessage());
        }
        return evaluations;
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            logger.warn("Tentative de mise à jour d'une évaluation complète null ou sans ID");
            return false;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            if (evaluation.getComment() != null) {
                stmt.setString(2, evaluation.getComment());
            } else {
                stmt.setNull(2, Types.CLOB);
            }
            stmt.setString(3, evaluation.getUsername());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            stmt.setInt(5, evaluation.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                GradeMapper.getInstance().deleteByEvaluationId(evaluation.getId());
                if (evaluation.getGrades() != null && !evaluation.getGrades().isEmpty()) {
                    for (Grade grade : evaluation.getGrades()) {
                        grade.setEvaluation(evaluation);
                        GradeMapper.getInstance().create(grade);
                    }
                }
                addToCache(evaluation);
                logger.info("Évaluation complète {} mise à jour avec succès", evaluation.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour de l'évaluation complète: {}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        return evaluation != null && deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try {
            GradeMapper.getInstance().deleteByEvaluationId(id);
            try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    removeFromCache(id);
                    logger.info("Évaluation complète {} supprimée avec succès", id);
                    return true;
                }
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression de l'évaluation complète: {}", ex.getMessage());
        }
        return false;
    }

    private CompleteEvaluation mapResultSetToCompleteEvaluation(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        Date visitDate = rs.getDate("date_eval");
        String comment = rs.getString("commentaire");
        String username = rs.getString("nom_utilisateur");
        Integer restaurantId = rs.getInt("fk_rest");
        Restaurant restaurant = RestaurantMapper.getInstance().findById(restaurantId);
        CompleteEvaluation evaluation = new CompleteEvaluation(id, visitDate, restaurant, comment, username);
        Set<Grade> grades = GradeMapper.getInstance().findByEvaluationId(id);
        for (Grade grade : grades) {
            grade.setEvaluation(evaluation);
        }
        evaluation.setGrades(grades);
        return evaluation;
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