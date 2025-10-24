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
 *
 * <p>Ce service gère deux types d'évaluations :</p>
 * <ul>
 *   <li><strong>Évaluations basiques :</strong> Simple like/dislike avec adresse IP</li>
 *   <li><strong>Évaluations complètes :</strong> Commentaire + notes détaillées par critères</li>
 * </ul>
 *
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Validation des données d'évaluation</li>
 *   <li>Création d'évaluations basiques et complètes</li>
 *   <li>Gestion des notes par critères</li>
 *   <li>Gestion transactionnelle (commit/rollback)</li>
 * </ul>
 *
 * <p><strong>Pattern utilisé :</strong> Singleton</p>
 *
 * <p><strong>Validation des notes :</strong><br>
 * Les notes doivent être comprises entre {@link Constants.Evaluation#MIN_GRADE}
 * et {@link Constants.Evaluation#MAX_GRADE}.</p>
 *
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre>
 * EvaluationService service = EvaluationService.getInstance();
 *
 * // Évaluation basique
 * BasicEvaluation like = service.addBasicEvaluation(restaurant, true, "192.168.1.1");
 *
 * // Évaluation complète
 * Map&lt;EvaluationCriteria, Integer&gt; grades = new HashMap&lt;&gt;();
 * grades.put(criteriaService, 5);
 * grades.put(criteriaCuisine, 4);
 * CompleteEvaluation eval = service.addCompleteEvaluation(
 *     restaurant, "John Doe", "Excellent !", grades
 * );
 * </pre>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see BasicEvaluation
 * @see CompleteEvaluation
 * @see EvaluationCriteria
 * @see Grade
 */
public class EvaluationService {

    private static final Logger logger = LogManager.getLogger(EvaluationService.class);
    private static EvaluationService instance;

    /**
     * Constructeur privé pour le pattern Singleton.
     * Empêche l'instanciation directe de la classe.
     */
    private EvaluationService() {
    }

    /**
     * Retourne l'instance unique du service (pattern Singleton).
     * Crée l'instance lors du premier appel (lazy initialization).
     *
     * @return L'instance unique de EvaluationService, jamais null
     */
    public static EvaluationService getInstance() {
        if (instance == null) {
            instance = new EvaluationService();
        }
        return instance;
    }

    /**
     * Ajoute une évaluation basique (like/dislike) pour un restaurant.
     *
     * <p>Une évaluation basique contient :</p>
     * <ul>
     *   <li>Un vote positif ou négatif (like/dislike)</li>
     *   <li>La date de l'évaluation (générée automatiquement)</li>
     *   <li>L'adresse IP de l'utilisateur</li>
     *   <li>Le restaurant évalué</li>
     * </ul>
     *
     * <p><strong>Gestion transactionnelle :</strong><br>
     * Cette méthode gère automatiquement la transaction avec commit en cas
     * de succès et rollback en cas d'erreur.</p>
     *
     * <p><strong>Validation :</strong></p>
     * <ul>
     *   <li>Le restaurant ne peut pas être null</li>
     *   <li>Le restaurant doit avoir un ID valide</li>
     *   <li>Le like ne peut pas être null</li>
     *   <li>L'adresse IP ne peut pas être null ou vide</li>
     * </ul>
     *
     * @param restaurant Le restaurant à évaluer (ne peut pas être null, doit avoir un ID)
     * @param like true pour un like, false pour un dislike (ne peut pas être null)
     * @param ipAddress L'adresse IP de l'utilisateur (ne peut pas être null ou vide)
     * @return L'évaluation créée avec son ID généré, ou null en cas d'erreur
     * @see BasicEvaluation
     * @see BasicEvaluationMapper#create(BasicEvaluation)
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
     * Ajoute une évaluation complète avec commentaire et notes détaillées.
     *
     * <p>Une évaluation complète contient :</p>
     * <ul>
     *   <li>Un nom d'utilisateur</li>
     *   <li>Un commentaire textuel</li>
     *   <li>La date de l'évaluation (générée automatiquement)</li>
     *   <li>Des notes (de 1 à 5) pour chaque critère d'évaluation</li>
     *   <li>Le restaurant évalué</li>
     * </ul>
     *
     * <p><strong>Validation des notes :</strong><br>
     * Toutes les notes doivent être comprises entre
     * {@link Constants.Evaluation#MIN_GRADE} et {@link Constants.Evaluation#MAX_GRADE}.
     * Si une note est invalide, l'évaluation complète est rejetée.</p>
     *
     * <p><strong>Gestion transactionnelle :</strong><br>
     * L'évaluation complète et toutes ses notes sont créées dans une seule transaction.
     * En cas d'erreur, un rollback complet est effectué.</p>
     *
     * <p><strong>Validation :</strong></p>
     * <ul>
     *   <li>Le restaurant ne peut pas être null et doit avoir un ID valide</li>
     *   <li>Le nom d'utilisateur ne peut pas être null ou vide</li>
     *   <li>Au moins une note doit être fournie</li>
     *   <li>Toutes les notes doivent être entre MIN_GRADE et MAX_GRADE</li>
     *   <li>Chaque critère doit avoir un ID valide</li>
     * </ul>
     *
     * <p><strong>Exemple d'utilisation :</strong></p>
     * <pre>
     * Map&lt;EvaluationCriteria, Integer&gt; grades = new HashMap&lt;&gt;();
     * grades.put(criteriaService, 5);
     * grades.put(criteriaCuisine, 4);
     * grades.put(criteriaCadre, 5);
     *
     * CompleteEvaluation eval = service.addCompleteEvaluation(
     *     restaurant,
     *     "Marie Dupont",
     *     "Excellent restaurant, service impeccable !",
     *     grades
     * );
     * </pre>
     *
     * @param restaurant Le restaurant à évaluer (ne peut pas être null, doit avoir un ID)
     * @param username Le nom d'utilisateur (ne peut pas être null ou vide)
     * @param comment Le commentaire de l'évaluation (peut être null)
     * @param grades Map des critères avec leurs notes (de MIN_GRADE à MAX_GRADE).
     *               Ne peut pas être null ou vide.
     * @return L'évaluation complète créée avec son ID généré, ou null en cas d'erreur
     * @see CompleteEvaluation
     * @see Grade
     * @see EvaluationCriteria
     * @see Constants.Evaluation#MIN_GRADE
     * @see Constants.Evaluation#MAX_GRADE
     * @see CompleteEvaluationMapper#create(CompleteEvaluation)
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
     * Récupère tous les critères d'évaluation disponibles dans le système.
     *
     * <p>Les critères d'évaluation sont utilisés pour noter les restaurants
     * dans les évaluations complètes. Exemples de critères :</p>
     * <ul>
     *   <li>Service</li>
     *   <li>Cuisine / Qualité de la nourriture</li>
     *   <li>Cadre / Ambiance</li>
     *   <li>Rapport qualité/prix</li>
     * </ul>
     *
     * <p>Les critères sont triés par ordre alphabétique.</p>
     *
     * @return Un Set de tous les critères d'évaluation disponibles.
     *         Retourne un Set vide si aucun critère n'existe. Jamais null.
     * @see EvaluationCriteria
     * @see EvaluationCriteriaMapper#findAll()
     */
    public Set<EvaluationCriteria> getAllEvaluationCriterias() {
        logger.info("Récupération de tous les critères d'évaluation");
        return EvaluationCriteriaMapper.getInstance().findAll();
    }
}