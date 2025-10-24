package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe helper pour faciliter les opérations de persistance complexes.
 * Fournit des méthodes de haut niveau qui orchestrent plusieurs Mappers.
 *
 * <p>Cette classe agit comme une façade pour simplifier les opérations
 * complexes impliquant plusieurs entités et relations. Elle est utilisée
 * principalement par la couche Service.</p>
 *
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Chargement d'entités avec leurs relations (lazy loading évité)</li>
 *   <li>Recherches complexes multi-critères</li>
 *   <li>Suppressions en cascade</li>
 *   <li>Orchestration de plusieurs Mappers</li>
 * </ul>
 *
 * <p><strong>Pattern utilisé :</strong> Façade (Facade Pattern)</p>
 *
 * <p><strong>Note :</strong> Toutes les méthodes sont statiques car cette classe
 * est purement utilitaire et n'a pas d'état.</p>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see RestaurantMapper
 * @see BasicEvaluationMapper
 * @see CompleteEvaluationMapper
 */
public class PersistenceHelper {

    /**
     * Charge un restaurant avec TOUTES ses évaluations (Basic + Complete).
     *
     * <p>Cette méthode résout le problème du lazy loading en chargeant explicitement
     * toutes les relations en une seule opération.</p>
     *
     * <p><strong>Étapes effectuées :</strong></p>
     * <ol>
     *   <li>Chargement du restaurant de base</li>
     *   <li>Chargement des évaluations basiques (likes/dislikes)</li>
     *   <li>Chargement des évaluations complètes (avec notes et critères)</li>
     *   <li>Fusion de toutes les évaluations dans le restaurant</li>
     * </ol>
     *
     * @param restaurantId L'ID du restaurant à charger
     * @return Le restaurant avec toutes ses évaluations, ou null si non trouvé
     * @see #loadEvaluationsForRestaurant(Restaurant)
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
     * Charge tous les restaurants avec TOUTES leurs évaluations.
     *
     * <p><strong>⚠️ Performance :</strong> Cette méthode peut être coûteuse
     * sur de grandes bases de données car elle charge tous les restaurants
     * et toutes leurs évaluations en mémoire.</p>
     *
     * <p><strong>Cas d'usage :</strong></p>
     * <ul>
     *   <li>Affichage de la liste complète des restaurants</li>
     *   <li>Export de données</li>
     *   <li>Statistiques globales</li>
     * </ul>
     *
     * @return Un Set de tous les restaurants avec leurs évaluations.
     *         Jamais null, peut être vide.
     */
    public static Set<Restaurant> loadAllRestaurantsWithEvaluations() {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findAll();

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Recherche les restaurants par nom (contient) avec toutes leurs évaluations.
     * La recherche est insensible à la casse.
     *
     * <p><strong>Exemple :</strong> searchRestaurantsByName("pizza") trouve
     * "Pizzeria Roma", "Pizza Hut", etc.</p>
     *
     * @param name Le nom partiel à rechercher (ne peut pas être null)
     * @return Un Set des restaurants correspondants avec leurs évaluations.
     *         Jamais null, peut être vide.
     * @see RestaurantMapper#findByNameContains(String)
     */
    public static Set<Restaurant> searchRestaurantsByName(String name) {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findByNameContains(name);

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Recherche les restaurants par ville avec toutes leurs évaluations.
     * La recherche est insensible à la casse et cherche une correspondance partielle.
     *
     * <p><strong>Algorithme :</strong></p>
     * <ol>
     *   <li>Charger tous les restaurants</li>
     *   <li>Filtrer ceux dont la ville contient le nom recherché</li>
     *   <li>Charger les évaluations des restaurants filtrés</li>
     * </ol>
     *
     * @param cityName Le nom partiel de la ville à rechercher (ne peut pas être null)
     * @return Un Set des restaurants correspondants avec leurs évaluations.
     *         Jamais null, peut être vide.
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
     * Recherche les restaurants par type avec toutes leurs évaluations.
     *
     * <p><strong>Exemples de types :</strong> Cuisine suisse, Pizzeria,
     * Restaurant gastronomique, etc.</p>
     *
     * @param type Le type de restaurant recherché (ne peut pas être null)
     * @return Un Set des restaurants de ce type avec leurs évaluations.
     *         Jamais null, peut être vide.
     * @see RestaurantMapper#findByType(RestaurantType)
     */
    public static Set<Restaurant> searchRestaurantsByType(RestaurantType type) {
        Set<Restaurant> restaurants = RestaurantMapper.getInstance().findByType(type);

        for (Restaurant restaurant : restaurants) {
            loadEvaluationsForRestaurant(restaurant);
        }

        return restaurants;
    }

    /**
     * Charge les évaluations (basiques et complètes) pour un restaurant donné.
     *
     * <p>Cette méthode est privée et utilisée par les autres méthodes publiques
     * pour éviter la duplication de code.</p>
     *
     * <p><strong>Opérations effectuées :</strong></p>
     * <ol>
     *   <li>Récupération des évaluations basiques (likes/dislikes)</li>
     *   <li>Récupération des évaluations complètes (avec notes)</li>
     *   <li>Fusion dans un seul Set</li>
     *   <li>Association au restaurant</li>
     * </ol>
     *
     * @param restaurant Le restaurant pour lequel charger les évaluations (ne peut pas être null)
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
     * Supprime complètement un restaurant et toutes ses évaluations.
     *
     * <p><strong>⚠️ ATTENTION :</strong> Cette opération est <strong>irréversible</strong>.</p>
     *
     * <p><strong>Ordre de suppression :</strong> (respecte les contraintes d'intégrité)</p>
     * <ol>
     *   <li>Suppression des notes (grades) via les évaluations complètes</li>
     *   <li>Suppression des évaluations complètes</li>
     *   <li>Suppression des évaluations basiques (likes/dislikes)</li>
     *   <li>Suppression du restaurant</li>
     * </ol>
     *
     * <p><strong>Gestion des erreurs :</strong><br>
     * En cas d'échec à n'importe quelle étape, la méthode continue et retourne false.
     * La gestion transactionnelle (commit/rollback) est gérée par la couche Service.</p>
     *
     * @param restaurant Le restaurant à supprimer (ne peut pas être null, doit avoir un ID)
     * @return true si toutes les suppressions ont réussi, false sinon
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
     * Charge toutes les villes disponibles dans le système.
     *
     * @return Un Set de toutes les villes. Jamais null, peut être vide.
     * @see CityMapper#findAll()
     */
    @SuppressWarnings("unused")
    public static Set<City> loadAllCities() {
        return CityMapper.getInstance().findAll();
    }

    /**
     * Charge tous les types de restaurants disponibles.
     *
     * @return Un Set de tous les types de restaurants. Jamais null, peut être vide.
     * @see RestaurantTypeMapper#findAll()
     */
    @SuppressWarnings("unused")
    public static Set<RestaurantType> loadAllRestaurantTypes() {
        return RestaurantTypeMapper.getInstance().findAll();
    }

    /**
     * Charge tous les critères d'évaluation disponibles.
     *
     * <p>Les critères sont utilisés pour noter les restaurants dans les
     * évaluations complètes. Exemples : Service, Cuisine, Cadre, etc.</p>
     *
     * @return Un Set de tous les critères d'évaluation. Jamais null, peut être vide.
     * @see EvaluationCriteriaMapper#findAll()
     */
    @SuppressWarnings("unused")
    public static Set<EvaluationCriteria> loadAllEvaluationCriterias() {
        return EvaluationCriteriaMapper.getInstance().findAll();
    }

}