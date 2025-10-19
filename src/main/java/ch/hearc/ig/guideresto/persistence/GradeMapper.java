package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class GradeMapper extends AbstractMapper<Grade> {

    private static GradeMapper instance;

    private GradeMapper() {}

    public static GradeMapper getInstance() {
        if (instance == null) {
            instance = new GradeMapper();
        }
        return instance;
    }

    private static final String FIND_BY_ID = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE numero = ?";
    private static final String FIND_ALL = "SELECT numero, note, fk_comm, fk_crit FROM NOTES";
    private static final String FIND_BY_EVALUATION = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE fk_comm = ?";
    private static final String INSERT = "INSERT INTO NOTES (note, fk_comm, fk_crit) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE NOTES SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM NOTES WHERE numero = ?";
    private static final String DELETE_BY_EVALUATION = "DELETE FROM NOTES WHERE fk_comm = ?";
    private static final String EXISTS = "SELECT 1 FROM NOTES WHERE numero = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM NOTES";
    private static final String SEQUENCE = "SELECT SEQ_NOTES.CURRVAL FROM DUAL";

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
                addToCache(grade);
                logger.info("Note créée avec succès (ID: {})", grade.getId());
                return grade;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    @Override
    protected Grade findByIdFromDb(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGrade(rs);
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
                addToCache(grade);
            }
            logger.info("{} notes chargées", grades.size());
        } catch (SQLException ex) {
            logger.error("Erreur lors du chargement des notes: {}", ex.getMessage());
        }
        return grades;
    }

    public Set<Grade> findByEvaluationId(int evaluationId) {
        Set<Grade> grades = new HashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_EVALUATION)) {
            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    grades.add(grade);
                    addToCache(grade);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche des notes pour l'évaluation {}: {}", evaluationId, ex.getMessage());
        }
        return grades;
    }

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
                addToCache(grade);
                logger.info("Note {} mise à jour avec succès", grade.getId());
                return true;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

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
                removeFromCache(id);
                logger.info("Note {} supprimée avec succès", id);
                return true;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    public boolean deleteByEvaluationId(int evaluationId) {
        Connection connection = ConnectionUtils.getConnection();
        try {
            Set<Grade> gradesToDelete = findByEvaluationId(evaluationId);
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_EVALUATION)) {
            stmt.setInt(1, evaluationId);
            int rowsAffected = stmt.executeUpdate();
            for (Grade grade : gradesToDelete) {
                removeFromCache(grade.getId());
            }
            logger.info("{} notes supprimées pour l'évaluation {}", rowsAffected, evaluationId);
            return true;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        int id = rs.getInt("numero");
        int gradeValue = rs.getInt("note");
        int criteriaId = rs.getInt("fk_crit");
        EvaluationCriteria criteria = EvaluationCriteriaMapper.getInstance().findById(criteriaId);
        return new Grade(id, gradeValue, null, criteria);
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