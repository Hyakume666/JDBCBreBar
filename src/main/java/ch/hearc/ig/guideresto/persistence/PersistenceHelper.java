package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe helper pour faciliter les opérations de persistance complexes
 */
public class PersistenceHelper {

    /**
     * Charge un restaurant avec TOUTES ses évaluations (Basic + Complete)
     */
    @SuppressWarnings("unused")
    public static Restaurant loadRestaurantWithEvaluations(int restaurantId) {
        Restaurant restaurant = RestaurantMapper.getInstance().findById(restaurantId);

        if (restaurant != null) {
            loadEvaluationsForRestaurant(restaurant);
        }
        return restaurant;
    }

    /**
     * Charge tous les restaurants avec TOUTES leurs évaluations
     */
    public static Set<Restaurant> loadAllRestaurantsWithEvaluations() {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findAll();

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Recherche les restaurants par nom (contient) avec toutes leurs évaluations
     */
    public static Set<Restaurant> searchRestaurantsByName(String name) {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findByNameContains(name);

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Recherche les restaurants par ville avec toutes leurs évaluations
     */
    public static Set<Restaurant> searchRestaurantsByCity(String cityName) {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findAll();
        Set<Restaurant> filtered = new HashSet<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant.getAddress().getCity().getCityName().toUpperCase()
                    .contains(cityName.toUpperCase())) {
                loadEvaluationsForRestaurant(restaurant);
                filtered.add(restaurant);
            }
        }

        return filtered;
    }

    /**
     * Recherche les restaurants par type avec toutes leurs évaluations
     */
    public static Set<Restaurant> searchRestaurantsByType(RestaurantType type) {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findByType(type);

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Charge les évaluations pour un restaurant donné
     */
    private static void loadEvaluationsForRestaurant(Restaurant restaurant) {
        Set<BasicEvaluation> basicEvaluations =
                BasicEvaluationMapper.getInstance().findByRestaurantId(restaurant.getId());

        Set<CompleteEvaluation> completeEvaluations =
                CompleteEvaluationMapper.getInstance().findByRestaurantId(restaurant.getId());

        Set<Evaluation> allEvaluations = new HashSet<>();
        allEvaluations.addAll(basicEvaluations);
        allEvaluations.addAll(completeEvaluations);

        restaurant.setEvaluations(allEvaluations);
    }

    /**
     * Supprime complètement un restaurant et toutes ses évaluations
     */
    public static boolean deleteRestaurantCompletely(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            return false;
        }
        Set<CompleteEvaluation> completeEvaluations =
                CompleteEvaluationMapper.getInstance().findByRestaurantId(restaurant.getId());

        for (CompleteEvaluation evaluation : completeEvaluations) {
            CompleteEvaluationMapper.getInstance().delete(evaluation);
        }

        Set<BasicEvaluation> basicEvaluations =
                BasicEvaluationMapper.getInstance().findByRestaurantId(restaurant.getId());

        for (BasicEvaluation evaluation : basicEvaluations) {
            BasicEvaluationMapper.getInstance().delete(evaluation);
        }

        return RestaurantMapper.getInstance().delete(restaurant);
    }

    /**
     * Charge toutes les villes
     */
    @SuppressWarnings("unused")
    public static Set<City> loadAllCities() {
        return CityMapper.getInstance().findAll();
    }

    /**
     * Charge tous les types de restaurants
     */
    @SuppressWarnings("unused")
    public static Set<RestaurantType> loadAllRestaurantTypes() {
        return RestaurantTypeMapper.getInstance().findAll();
    }

    /**
     * Charge tous les critères d'évaluation
     */
    @SuppressWarnings("unused")
    public static Set<EvaluationCriteria> loadAllEvaluationCriterias() {
        return EvaluationCriteriaMapper.getInstance().findAll();
    }

}