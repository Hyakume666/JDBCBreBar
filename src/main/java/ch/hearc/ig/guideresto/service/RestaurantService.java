package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class RestaurantService {

    private static final Logger logger = LogManager.getLogger(RestaurantService.class);

    // Singleton
    private static RestaurantService instance;

    private RestaurantService() {
    }

    public static RestaurantService getInstance() {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }

    /**
     * Récupère tous les restaurants avec leurs évaluations.
     * @return Un Set de tous les restaurants avec leurs évaluations.
     */
    public Set<Restaurant> getAllRestaurantsWithEvaluations() {
        logger.info("Récupération de tous les restaurants avec évaluations");
        return PersistenceHelper.loadAllRestaurantsWithEvaluations();
    }

    /**
     * Recherche des restaurants par nom, incluant leurs évaluations.
     * @param name Le nom partiel à rechercher.
     * @return Un Set des restaurants correspondants.
     */
    public Set<Restaurant> searchRestaurantsByName(String name) {
        logger.info("Recherche de restaurants par nom : {}", name);
        return PersistenceHelper.searchRestaurantsByName(name);
    }

    /**
     * Recherche des restaurants par nom de ville, incluant leurs évaluations.
     * @param cityName Le nom partiel de la ville à rechercher.
     * @return Un Set des restaurants correspondants.
     */
    public Set<Restaurant> searchRestaurantsByCity(String cityName) {
        logger.info("Recherche de restaurants par ville : {}", cityName);
        return PersistenceHelper.searchRestaurantsByCity(cityName);
    }

    /**
     * Recherche des restaurants par type, incluant leurs évaluations.
     * @param type Le type de restaurant.
     * @return Un Set des restaurants correspondants.
     */
    public Set<Restaurant> searchRestaurantsByType(RestaurantType type) {
        logger.info("Recherche de restaurants par type : {}", type.getLabel());
        return PersistenceHelper.searchRestaurantsByType(type);
    }

    /**
     * Crée un nouveau restaurant.
     * Gère la transaction complète avec commit/rollback.
     * @param restaurant Le restaurant qui va être créé.
     * @return Le restaurant créé avec son ID, ou null en cas d'erreur.
     */
    public Restaurant createRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Tentative de création d'un restaurant null");
            return null;
        }

        logger.info("Création d'un nouveau restaurant : {}", restaurant.getName());
        Connection connection = ConnectionUtils.getConnection();

        try {
            Restaurant created = RestaurantMapper.getInstance().create(restaurant);

            if (created != null) {
                connection.commit();
                logger.info("Restaurant créé avec succès (ID: {})", created.getId());
                return created;
            } else {
                connection.rollback();
                logger.error("Échec de la création du restaurant");
                return null;
            }
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return null;
        }
    }

    /**
     * Met à jour un restaurant existant.
     * Gère la transaction complète avec commit/rollback.
     * @param restaurant Le restaurant à mettre à jour.
     * @return true si la mise à jour a réussi, false sinon.
     */
    public boolean updateRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative de mise à jour d'un restaurant null ou sans ID");
            return false;
        }

        logger.info("Mise à jour du restaurant ID {}", restaurant.getId());
        Connection connection = ConnectionUtils.getConnection();

        try {
            boolean success = RestaurantMapper.getInstance().update(restaurant);

            if (success) {
                connection.commit();
                logger.info("Restaurant mis à jour avec succès");
            } else {
                connection.rollback();
                logger.warn("Échec de la mise à jour du restaurant");
            }

            return success;
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return false;
        }
    }

    /**
     * Supprime complètement un restaurant et toutes ses évaluations.
     * Gère la transaction complète avec suppression en cascade
     * @param restaurant Le restaurant à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean deleteRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            logger.warn("Tentative de suppression d'un restaurant null ou sans ID");
            return false;
        }

        logger.info("Suppression du restaurant ID {} : {}", restaurant.getId(), restaurant.getName());
        Connection connection = ConnectionUtils.getConnection();

        try {
            boolean success = PersistenceHelper.deleteRestaurantCompletely(restaurant);

            if (success) {
                connection.commit();
                logger.info("Restaurant supprimé avec succès");
            } else {
                connection.rollback();
                logger.warn("Échec de la suppression du restaurant");
            }

            return success;
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return false;
        }
    }

    /**
     * Ajoute une évaluation basique (like/dislike) pour un restaurant.
     * Gère la transaction.
     * @param restaurant Le restaurant a évalué.
     * @param like true pour un like, false pour un dislike.
     * @param ipAddress L'adresse IP de l'utilisateur.
     * @return L'évaluation créée, ou null en cas d'erreur.
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
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return null;
        }
    }

    /**
     * Ajoute une évaluation complète avec commentaire et notes pour un restaurant.
     * Gère la transaction complète (évaluation + notes).
     * @param restaurant Le restaurant a évalué.
     * @param username Le nom d'utilisateur.
     * @param comment Le commentaire.
     * @param grades Map des critères avec leurs notes (de 1 à 5).
     * @return L'évaluation complète créée, ou null en cas d'erreur.
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
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return null;
        }
    }

    /**
     * Récupère toutes les villes.
     * @return Un Set de toutes les villes.
     */
    public Set<City> getAllCities() {
        logger.info("Récupération de toutes les villes");
        return CityMapper.getInstance().findAll();
    }

    /**
     * Crée une nouvelle ville.
     * Gère la transaction.
     * @param city La ville à créer.
     * @return La ville créée avec son ID, ou null en cas d'erreur.
     */
    public City createCity(City city) {
        if (city == null) {
            logger.warn("Tentative de création d'une ville null");
            return null;
        }

        logger.info("Création d'une nouvelle ville : {} ({})", city.getCityName(), city.getZipCode());
        Connection connection = ConnectionUtils.getConnection();

        try {
            City created = CityMapper.getInstance().create(city);

            if (created != null) {
                connection.commit();
                logger.info("Ville créée avec succès (ID: {})", created.getId());
                return created;
            } else {
                connection.rollback();
                logger.error("Échec de la création de la ville");
                return null;
            }
        } catch (SQLException ex) {
            rollbackAndLog(connection, ex);
            return null;
        }
    }

    /**
     * Récupère tous les types de restaurants.
     * @return Un Set de tous les types.
     */
    public Set<RestaurantType> getAllRestaurantTypes() {
        logger.info("Récupération de tous les types de restaurants");
        return RestaurantTypeMapper.getInstance().findAll();
    }

    /**
     * Récupère tous les critères d'évaluation.
     * @return Un Set de tous les critères.
     */
    public Set<EvaluationCriteria> getAllEvaluationCriterias() {
        logger.info("Récupération de tous les critères d'évaluation");
        return EvaluationCriteriaMapper.getInstance().findAll();
    }
    /**
     * Effectue un rollback et log l'erreur
     */
    private void rollbackAndLog(Connection connection, SQLException ex) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.error("Erreur lors du rollback: {}", e.getMessage());
        }
        logger.error("Erreur SQL: {}", ex.getMessage());
    }
}