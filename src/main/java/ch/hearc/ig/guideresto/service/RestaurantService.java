package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Service dédié à la gestion des restaurants et entités associées.
 * Centralise la logique métier et la gestion des transactions.
 */
public class RestaurantService {

    private static final Logger logger = LogManager.getLogger(RestaurantService.class);
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
     * @return Un Set de tous les restaurants avec leurs évaluations
     */
    public Set<Restaurant> getAllRestaurantsWithEvaluations() {
        logger.info("Récupération de tous les restaurants avec évaluations");
        return PersistenceHelper.loadAllRestaurantsWithEvaluations();
    }

    /**
     * Recherche des restaurants par nom, incluant leurs évaluations.
     * @param name Le nom partiel à rechercher
     * @return Un Set des restaurants correspondants
     */
    public Set<Restaurant> searchRestaurantsByName(String name) {
        logger.info("Recherche de restaurants par nom : {}", name);
        return PersistenceHelper.searchRestaurantsByName(name);
    }

    /**
     * Recherche des restaurants par nom de ville, incluant leurs évaluations.
     * @param cityName Le nom partiel de la ville à rechercher
     * @return Un Set des restaurants correspondants
     */
    public Set<Restaurant> searchRestaurantsByCity(String cityName) {
        logger.info("Recherche de restaurants par ville : {}", cityName);
        return PersistenceHelper.searchRestaurantsByCity(cityName);
    }

    /**
     * Recherche des restaurants par type, incluant leurs évaluations.
     * @param type Le type de restaurant
     * @return Un Set des restaurants correspondants
     */
    public Set<Restaurant> searchRestaurantsByType(RestaurantType type) {
        logger.info("Recherche de restaurants par type : {}", type.getLabel());
        return PersistenceHelper.searchRestaurantsByType(type);
    }

    /**
     * Crée un nouveau restaurant.
     * @param restaurant Le restaurant à créer
     * @return Le restaurant créé avec son ID, ou null en cas d'erreur
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
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erreur lors du rollback: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création du restaurant: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Met à jour un restaurant existant.
     * @param restaurant Le restaurant à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
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
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback échoué: {}", e.getMessage());
            }
            logger.error("Erreur lors de la mise à jour du restaurant: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Supprime complètement un restaurant et toutes ses évaluations.
     * @param restaurant Le restaurant à supprimer
     * @return true si la suppression a réussi, false sinon
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
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback échoué: {}", e.getMessage());
            }
            logger.error("Erreur lors de la suppression du restaurant: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Récupère toutes les villes.
     * @return Un Set de toutes les villes
     */
    public Set<City> getAllCities() {
        logger.info("Récupération de toutes les villes");
        return CityMapper.getInstance().findAll();
    }

    /**
     * Crée une nouvelle ville.
     * @param city La ville à créer
     * @return La ville créée avec son ID, ou null en cas d'erreur
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
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback échoué: {}", e.getMessage());
            }
            logger.error("Erreur lors de la création de la ville: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Récupère tous les types de restaurants.
     * @return Un Set de tous les types
     */
    public Set<RestaurantType> getAllRestaurantTypes() {
        logger.info("Récupération de tous les types de restaurants");
        return RestaurantTypeMapper.getInstance().findAll();
    }
}