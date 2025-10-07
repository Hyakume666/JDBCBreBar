package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Mapper pour la classe Grade
 * Gère la persistance des notes dans la table NOTES
 */
public class GradeMapper extends AbstractMapper<Grade> {

    // CACHE
    private static final java.util.Map<Integer, Grade> cache = new java.util.HashMap<>();

    // SINGLETON
    private static GradeMapper instance;

    private GradeMapper() {
    }

    public static GradeMapper getInstance() {
        if (instance == null) {
            instance = new GradeMapper();
        }
        return instance;
    }

    // REQUÊTES SQL
    private static final String FIND_BY_ID =
            "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE numero = ?";

    private static final String FIND_ALL =
            "SELECT numero, note, fk_comm, fk_crit FROM NOTES";

    private static final String FIND_BY_EVALUATION =
            "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE fk_comm = ?";

    private static final String INSERT =
            "INSERT INTO NOTES (note, fk_comm, fk_crit) VALUES (?, ?, ?)";

    private static final String UPDATE =
            "UPDATE NOTES SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";

    private static final String DELETE = "DELETE FROM NOTES WHERE numero = ?";

    private static final String DELETE_BY_EVALUATION = "DELETE FROM NOTES WHERE fk_comm = ?";

    private static final String EXISTS = "SELECT 1 FROM NOTES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM NOTES";
    private static final String SEQUENCE = "SELECT SEQ_NOTES.CURRVAL FROM DUAL";

    // CREATE (INSERT)
    @Override
    public Grade create(Grade grade) {
        if (grade == null) {
            logger.warn("Tentative de création d'une note null");
            return null;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, grade.getGrade());
            stmt.setInt(2, grade.getEvaluation().getId());
            stmt.setInt(3, grade.getCriteria().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                grade.setId(getSequenceValue());
                connection.commit();
                cache.put(grade.getId(), grade);
                logger.info("Note créée avec succès (ID: {})", grade.getId());
                return grade;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création de la note: {}", ex.getMessage());
        }
        return null;
    }

    // READ (SELECT)
    @Override
    public Grade findById(int id) {
        if (cache.containsKey(id)) {
            logger.debug("Note {} trouvée dans le cache", id);
            return cache.get(id);
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    cache.put(id, grade);
                    return grade;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de la note {}: {}", id, ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<Grade> findAll() {
        Set<Grade> grades = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Grade grade = mapResultSetToGrade(rs);
                grades.add(grade);
                cache.put(grade.getId(), grade);
            }
            logger.info("{} notes chargées", grades.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des notes: {}", ex.getMessage());
        }
        return grades;
    }

    /**
     * Recherche toutes les notes d'une évaluation complète
     */
    public Set<Grade> findByEvaluation(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            return new HashSet<>();
        }
        return findByEvaluationId(evaluation.getId());
    }

    /**
     * Recherche toutes les notes par ID d'évaluation
     */
    public Set<Grade> findByEvaluationId(int evaluationId) {
        Set<Grade> grades = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_EVALUATION)) {
            stmt.setInt(1, evaluationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    grades.add(grade);
                    cache.put(grade.getId(), grade);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche des notes pour l'évaluation {}: {}", evaluationId, ex.getMessage());
        }
        return grades;
    }

    // UPDATE
    @Override
    public boolean update(Grade grade) {
        if (grade == null || grade.getId() == null) {
            logger.warn("Tentative de mise à jour d'une note null ou sans ID");
            return false;
        }

        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, grade.getGrade());
            stmt.setInt(2, grade.getEvaluation().getId());
            stmt.setInt(3, grade.getCriteria().getId());
            stmt.setInt(4, grade.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                cache.put(grade.getId(), grade);
                logger.info("Note {} mise à jour avec succès", grade.getId());
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour de la note: {}", ex.getMessage());
        }
        return false;
    }

    // DELETE
    @Override
    public boolean delete(Grade grade) {
        return grade != null && deleteById(grade.getId());
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
                logger.info("Note {} supprimée avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression de la note: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Supprime toutes les notes d'une évaluation
     * Utile lors de la suppression d'une évaluation complète
     */
    public boolean deleteByEvaluationId(int evaluationId) {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_EVALUATION)) {
            stmt.setInt(1, evaluationId);

            int rowsAffected = stmt.executeUpdate();
            connection.commit();
            cache.entrySet().removeIf(entry ->
                    entry.getValue().getEvaluation() != null &&
                            entry.getValue().getEvaluation().getId().equals(evaluationId)
            );

            logger.info("{} notes supprimées pour l'évaluation {}", rowsAffected, evaluationId);
            return true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression des notes de l'évaluation: {}", ex.getMessage());
        }
        return false;
    }

    // MÉTHODES UTILITAIRES

    /**
     * Convertit une ligne de ResultSet en objet Grade
     * ATTENTION: Ne charge PAS l'évaluation complète pour éviter une boucle infinie
     * L'évaluation doit être définie après coup
     */
    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        Integer gradeValue = rs.getInt("note");
        Integer evaluationId = rs.getInt("fk_comm");
        Integer criteriaId = rs.getInt("fk_crit");

        EvaluationCriteria criteria = EvaluationCriteriaMapper.getInstance().findById(criteriaId);
        Grade grade = new Grade(id, gradeValue, null, criteria);

        return grade;
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
        logger.debug("Cache des notes vidé");
    }

    @Override
    protected void addToCache(Grade grade) {
        if (grade != null && grade.getId() != null) {
            cache.put(grade.getId(), grade);
        }
    }

    @Override
    protected void removeFromCache(Integer id) {
        cache.remove(id);
    }
}