package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Service dédié à la gestion des évaluations (basiques et complètes).
 * Sépare les responsabilités selon le principe SOLID.
 */
public class EvaluationService {

    private static final Logger logger = LogManager.getLogger(EvaluationService.class);
    private static EvaluationService instance;

    private EvaluationService() {
    }

    public static EvaluationService getInstance() {
        if (instance == null) {
            instance = new EvaluationService();
        }
        return instance;
    }

    /**
     * Ajoute une évaluation basique (like/dislike) pour un restaurant.
     * Gère la transaction complète avec commit/rollback.
     * @param restaurant Le restaurant à évaluer
     * @param like true pour un like, false pour un dislike
     * @param ipAddress L'adresse IP de l'utilisateur
     * @return L'évaluation créée, ou null en cas d'erreur
     */
    public BasicEvaluation addBasicEvaluation(Restaurant restaurant, Boolean like, String ipAddress) {
        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative d'ajout d'évaluation basique sur un restaurant null ou sans ID");
            return null;
        }

        logger.info("Ajout d'une évaluation basique ({}) pour le restaurant ID {}",
                like ? "LIKE" : "DISLIKE", restaurant.getId());

        Connection connection = ConnectionUtils.getConnection();

        try {
            BasicEvaluation evaluation = new BasicEvaluation(
                    null,
                    new Date(),
                    restaurant,
                    like,
                    ipAddress
            );

            BasicEvaluation created = BasicEvaluationMapper.getInstance().create(evaluation);

            if (created != null) {
                connection.commit();
                logger.info("Évaluation basique créée avec succès (ID: {})", created.getId());
                return created;
            } else {
                connection.rollback();
                logger.error("Échec de la création de l'évaluation basique");
                return null;
            }
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback échoué: {}", e.getMessage());
            }
            logger.error("Erreur lors de l'ajout de l'évaluation basique: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Ajoute une évaluation complète avec commentaire et notes.
     * Valide que toutes les notes sont entre MIN_GRADE et MAX_GRADE.
     * Gère la transaction complète avec commit/rollback.
     * @param restaurant Le restaurant à évaluer
     * @param username Le nom d'utilisateur
     * @param comment Le commentaire
     * @param grades Map des critères avec leurs notes (de MIN_GRADE à MAX_GRADE)
     * @return L'évaluation complète créée, ou null en cas d'erreur
     */
    public CompleteEvaluation addCompleteEvaluation(
            Restaurant restaurant,
            String username,
            String comment,
            Map<EvaluationCriteria, Integer> grades) {

        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative d'ajout d'évaluation complète sur un restaurant null ou sans ID");
            return null;
        }

        if (username == null || username.trim().isEmpty()) {
            logger.warn("Tentative d'ajout d'évaluation complète sans nom d'utilisateur");
            return null;
        }

        // Validation des notes avec les constantes
        for (Integer grade : grades.values()) {
            if (grade < Constants.Evaluation.MIN_GRADE || grade > Constants.Evaluation.MAX_GRADE) {
                logger.error("Note invalide détectée: {}. Les notes doivent être entre {} et {}",
                        grade, Constants.Evaluation.MIN_GRADE, Constants.Evaluation.MAX_GRADE);
                return null;
            }
        }

        logger.info("Ajout d'une évaluation complète pour le restaurant ID {} par {}",
                restaurant.getId(), username);

        Connection connection = ConnectionUtils.getConnection();

        try {
            CompleteEvaluation evaluation = new CompleteEvaluation(
                    null,
                    new Date(),
                    restaurant,
                    comment,
                    username
            );

            for (Map.Entry<EvaluationCriteria, Integer> entry : grades.entrySet()) {
                Grade grade = new Grade(null, entry.getValue(), evaluation, entry.getKey());
                evaluation.getGrades().add(grade);
            }

            CompleteEvaluation created = CompleteEvaluationMapper.getInstance().create(evaluation);

            if (created != null) {
                connection.commit();
                logger.info("Évaluation complète créée avec succès (ID: {}) avec {} notes",
                        created.getId(), created.getGrades().size());
                return created;
            } else {
                connection.rollback();
                logger.error("Échec de la création de l'évaluation complète");
                return null;
            }
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback échoué: {}", e.getMessage());
            }
            logger.error("Erreur lors de l'ajout de l'évaluation complète: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Récupère tous les critères d'évaluation disponibles.
     * @return Un Set de tous les critères
     */
    public Set<EvaluationCriteria> getAllEvaluationCriterias() {
        logger.info("Récupération de tous les critères d'évaluation");
        return EvaluationCriteriaMapper.getInstance().findAll();
    }
}