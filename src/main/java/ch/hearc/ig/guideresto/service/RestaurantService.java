package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Service de gestion des restaurants et entités associées (villes, types).
 * Centralise toute la logique métier liée aux restaurants.
 *
 * <p>Ce service gère les transactions et utilise les Mappers pour la persistance.
 * Il assure que toutes les opérations CRUD sont encapsulées dans des transactions
 * avec gestion automatique du commit/rollback.</p>
 *
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Création, modification, suppression de restaurants</li>
 *   <li>Recherche de restaurants (par nom, ville, type)</li>
 *   <li>Gestion des villes et types de restaurants</li>
 *   <li>Gestion transactionnelle (commit/rollback)</li>
 * </ul>
 *
 * <p><strong>Pattern utilisé :</strong> Singleton</p>
 *
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre>
 * RestaurantService service = RestaurantService.getInstance();
 * Set&lt;Restaurant&gt; restaurants = service.getAllRestaurantsWithEvaluations();
 *
 * Restaurant newRestaurant = new Restaurant(...);
 * Restaurant created = service.createRestaurant(newRestaurant);
 * </pre>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see Restaurant
 * @see City
 * @see RestaurantType
 * @see RestaurantMapper
 */
public class RestaurantService {

    private static final Logger logger = LogManager.getLogger(RestaurantService.class);
    private static RestaurantService instance;

    /**
     * Constructeur privé pour le pattern Singleton.
     * Empêche l'instanciation directe de la classe.
     */
    private RestaurantService() {
    }

    /**
     * Retourne l'instance unique du service (pattern Singleton).
     * Crée l'instance lors du premier appel (lazy initialization).
     *
     * @return L'instance unique de RestaurantService, jamais null
     */
    public static RestaurantService getInstance() {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }

    /**
     * Récupère tous les restaurants avec leurs évaluations complètes.
     *
     * <p>Cette méthode charge en mémoire tous les restaurants ainsi que
     * leurs évaluations basiques (likes/dislikes) et complètes (notes et commentaires).
     * Utiliser avec précaution sur de grandes bases de données.</p>
     *
     * <p><strong>Performance :</strong> Cette méthode effectue plusieurs requêtes SQL
     * pour charger les relations. Sur une grande base, préférer les méthodes de recherche
     * ciblées.</p>
     *
     * @return Un Set contenant tous les restaurants avec leurs évaluations.
     *         Retourne un Set vide si aucun restaurant n'existe. Jamais null.
     * @see PersistenceHelper#loadAllRestaurantsWithEvaluations()
     */
    public Set<Restaurant> getAllRestaurantsWithEvaluations() {
        logger.info("Récupération de tous les restaurants avec évaluations");
        return PersistenceHelper.loadAllRestaurantsWithEvaluations();
    }

    /**
     * Recherche des restaurants dont le nom contient la chaîne spécifiée.
     * La recherche est insensible à la casse.
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *   <li>searchRestaurantsByName("pizza") → trouve "Pizzeria Roma", "Pizza Hut"</li>
     *   <li>searchRestaurantsByName("FLEUR") → trouve "Fleur-de-Lys"</li>
     * </ul>
     *
     * @param name Le nom partiel à rechercher (ne peut pas être null ou vide)
     * @return Un Set des restaurants correspondants avec leurs évaluations.
     *         Retourne un Set vide si aucun restaurant ne correspond. Jamais null.
     * @throws IllegalArgumentException si name est null
     * @see PersistenceHelper#searchRestaurantsByName(String)
     */
    public Set<Restaurant> searchRestaurantsByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Le nom de recherche ne peut pas être null");
        }
        logger.info("Recherche de restaurants par nom : {}", name);
        return PersistenceHelper.searchRestaurantsByName(name);
    }

    /**
     * Recherche des restaurants par nom de ville.
     * La recherche est insensible à la casse et cherche une correspondance partielle.
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *   <li>searchRestaurantsByCity("Neuch") → trouve les restaurants de "Neuchâtel"</li>
     *   <li>searchRestaurantsByCity("GENEVE") → trouve les restaurants de "Genève"</li>
     * </ul>
     *
     * @param cityName Le nom partiel de la ville à rechercher (ne peut pas être null)
     * @return Un Set des restaurants correspondants avec leurs évaluations.
     *         Retourne un Set vide si aucun restaurant ne correspond. Jamais null.
     * @throws IllegalArgumentException si cityName est null
     * @see PersistenceHelper#searchRestaurantsByCity(String)
     */
    public Set<Restaurant> searchRestaurantsByCity(String cityName) {
        if (cityName == null) {
            throw new IllegalArgumentException("Le nom de la ville ne peut pas être null");
        }
        logger.info("Recherche de restaurants par ville : {}", cityName);
        return PersistenceHelper.searchRestaurantsByCity(cityName);
    }

    /**
     * Recherche des restaurants par type de cuisine.
     *
     * <p><strong>Exemples de types :</strong> Cuisine suisse, Pizzeria,
     * Restaurant gastronomique, etc.</p>
     *
     * @param type Le type de restaurant recherché (ne peut pas être null)
     * @return Un Set des restaurants de ce type avec leurs évaluations.
     *         Retourne un Set vide si aucun restaurant ne correspond. Jamais null.
     * @throws IllegalArgumentException si type est null
     * @see PersistenceHelper#searchRestaurantsByType(RestaurantType)
     * @see RestaurantType
     */
    public Set<Restaurant> searchRestaurantsByType(RestaurantType type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de restaurant ne peut pas être null");
        }
        logger.info("Recherche de restaurants par type : {}", type.getLabel());
        return PersistenceHelper.searchRestaurantsByType(type);
    }

    /**
     * Crée un nouveau restaurant dans le système.
     *
     * <p>Cette méthode effectue les opérations suivantes :</p>
     * <ol>
     *   <li>Validation du restaurant (non null)</li>
     *   <li>Insertion en base de données via RestaurantMapper</li>
     *   <li>Génération automatique de l'ID par la séquence Oracle</li>
     *   <li>Commit de la transaction si succès</li>
     *   <li>Rollback automatique si erreur</li>
     * </ol>
     *
     * <p><strong>Pré-requis :</strong></p>
     * <ul>
     *   <li>Le restaurant doit avoir un nom non vide</li>
     *   <li>L'adresse et la ville doivent être définies</li>
     *   <li>Le type de restaurant doit être défini et avoir un ID valide</li>
     *   <li>La ville et le type doivent exister en base</li>
     * </ul>
     *
     * @param restaurant Le restaurant à créer (ne peut pas être null)
     * @return Le restaurant créé avec son ID généré, ou null en cas d'échec
     * @see RestaurantMapper#create(Restaurant)
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
     *
     * <p>Toutes les informations du restaurant sont mises à jour en base,
     * y compris le nom, la description, l'adresse, le site web et le type.</p>
     *
     * <p><strong>Attention :</strong> Les évaluations du restaurant ne sont pas
     * modifiées par cette méthode. Pour gérer les évaluations, utiliser
     * {@link EvaluationService}.</p>
     *
     * <p><strong>Gestion transactionnelle :</strong></p>
     * <ul>
     *   <li>Commit automatique si succès</li>
     *   <li>Rollback automatique si erreur</li>
     * </ul>
     *
     * @param restaurant Le restaurant à mettre à jour (ne peut pas être null,
     *                   doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @see RestaurantMapper#update(Restaurant)
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
     * Supprime complètement un restaurant et toutes ses évaluations associées.
     *
     * <p><strong>⚠️ ATTENTION :</strong> Cette opération est <strong>irréversible</strong>
     * et supprime également :</p>
     * <ul>
     *   <li>Toutes les évaluations complètes du restaurant</li>
     *   <li>Toutes les notes associées aux évaluations complètes</li>
     *   <li>Toutes les évaluations basiques (likes/dislikes)</li>
     * </ul>
     *
     * <p><strong>Ordre de suppression :</strong> (respecte les contraintes d'intégrité)</p>
     * <ol>
     *   <li>Suppression des notes (grades) des évaluations complètes</li>
     *   <li>Suppression des évaluations complètes</li>
     *   <li>Suppression des évaluations basiques</li>
     *   <li>Suppression du restaurant</li>
     * </ol>
     *
     * <p><strong>Gestion transactionnelle :</strong><br>
     * Toutes ces opérations sont effectuées dans une seule transaction.
     * En cas d'erreur, un rollback complet est effectué.</p>
     *
     * @param restaurant Le restaurant à supprimer (ne peut pas être null,
     *                   doit avoir un ID valide)
     * @return true si la suppression a réussi, false sinon
     * @see PersistenceHelper#deleteRestaurantCompletely(Restaurant)
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
     * Récupère toutes les villes disponibles dans le système.
     *
     * <p>Les villes sont triées par ordre alphabétique du nom de ville.</p>
     *
     * @return Un Set de toutes les villes. Retourne un Set vide si aucune ville
     *         n'existe. Jamais null.
     * @see CityMapper#findAll()
     * @see City
     */
    public Set<City> getAllCities() {
        logger.info("Récupération de toutes les villes");
        return CityMapper.getInstance().findAll();
    }

    /**
     * Crée une nouvelle ville dans le système.
     *
     * <p><strong>Validations effectuées :</strong></p>
     * <ul>
     *   <li>La ville ne doit pas être null</li>
     *   <li>Le NPA doit être composé de 4 chiffres</li>
     *   <li>Le nom de la ville est obligatoire</li>
     * </ul>
     *
     * <p><strong>Exemple d'utilisation :</strong></p>
     * <pre>
     * City city = new City("2000", "Neuchâtel");
     * City created = service.createCity(city);
     * if (created != null) {
     *     System.out.println("Ville créée avec ID: " + created.getId());
     * }
     * </pre>
     *
     * @param city La ville à créer (ne peut pas être null)
     * @return La ville créée avec son ID généré, ou null en cas d'échec
     * @see CityMapper#create(City)
     * @see City
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
     * Récupère tous les types de restaurants disponibles.
     *
     * <p>Les types sont triés par ordre alphabétique du libellé.</p>
     *
     * <p><strong>Exemples de types :</strong> Cuisine suisse, Pizzeria,
     * Restaurant gastronomique, Fast-food, Cuisine asiatique, etc.</p>
     *
     * @return Un Set de tous les types de restaurants.
     *         Retourne un Set vide si aucun type n'existe. Jamais null.
     * @see RestaurantTypeMapper#findAll()
     * @see RestaurantType
     */
    public Set<RestaurantType> getAllRestaurantTypes() {
        logger.info("Récupération de tous les types de restaurants");
        return RestaurantTypeMapper.getInstance().findAll();
    }
}