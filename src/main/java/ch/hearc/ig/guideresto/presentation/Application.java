package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.service.RestaurantService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author cedric.baudet
 * @author alain.matile
 * VERSION SERVICE LAYER - Utilise RestaurantService au lieu des Mappers
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static final RestaurantService restaurantService = RestaurantService.getInstance();

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;
        do {
            printMainMenu();
            choice = readInt();
            proceedMainMenu(choice);
        } while (choice != 0);

        // Fermer la connexion à la fin
        ConnectionUtils.closeConnection();
        System.out.println("Connexion fermée. À bientôt !");
    }

    /**
     * Affichage du menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice) {
        switch (choice) {
            case 1:
                showRestaurantsList();
                break;
            case 2:
                searchRestaurantByName();
                break;
            case 3:
                searchRestaurantByCity();
                break;
            case 4:
                searchRestaurantByType();
                break;
            case 5:
                addNewRestaurant();
                break;
            case 0:
                System.out.println("Au revoir !");
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * On affiche à l'utilisateur une liste de restaurants numérotés, et il doit en sélectionner un
     * @param restaurants Liste à afficher
     * @return L'instance du restaurant choisi par l'utilisateur
     */
    private static Restaurant pickRestaurant(Set<Restaurant> restaurants) {
        if (restaurants.isEmpty()) {
            System.out.println("Aucun restaurant n'a été trouvé !");
            return null;
        }

        String result;
        for (Restaurant currentRest : restaurants) {
            result = "";
            result = "\"" + result + currentRest.getName() + "\" - " + currentRest.getAddress().getStreet() + " - ";
            result = result + currentRest.getAddress().getCity().getZipCode() + " " + currentRest.getAddress().getCity().getCityName();
            System.out.println(result);
        }

        System.out.println("Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
        String choice = readString();

        return searchRestaurantByName(restaurants, choice);
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(restaurantService.getAllRestaurantsWithEvaluations());

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();

        Set<Restaurant> filteredList = restaurantService.searchRestaurantsByName(research);

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom de la ville contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByCity() {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();
        Set<Restaurant> filteredList = restaurantService.searchRestaurantsByCity(research);

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * L'utilisateur choisit une ville parmi celles présentes dans le système.
     * @param cities La liste des villes à présenter à l'utilisateur
     * @return La ville sélectionnée, ou null si aucune ville n'a été choisie.
     */
    private static City pickCity(Set<City> cities) {
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();

        if (choice.equals("NEW")) {
            City city = new City();
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            city.setZipCode(readString());
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            city.setCityName(readString());

            city = restaurantService.createCity(city);

            return city;
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * L'utilisateur choisit un type de restaurant parmi ceux présents dans le système.
     * @param types La liste des types de restaurant à présenter à l'utilisateur
     * @return Le type sélectionné, ou null si aucun type n'a été choisi.
     */
    private static RestaurantType pickRestaurantType(Set<RestaurantType> types) {
        System.out.println("Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
        for (RestaurantType currentType : types) {
            System.out.println("\"" + currentType.getLabel() + "\" : " + currentType.getDescription());
        }
        String choice = readString();

        return searchTypeByLabel(types, choice);
    }

    /**
     * L'utilisateur commence par sélectionner un type de restaurant, puis sélectionne un des restaurants proposés s'il y en a.
     * Si l'utilisateur sélectionne un restaurant, ce dernier lui sera affiché.
     */
    private static void searchRestaurantByType() {
        RestaurantType chosenType = pickRestaurantType(restaurantService.getAllRestaurantTypes());

        if (chosenType != null) {
            Set<Restaurant> filteredList = restaurantService.searchRestaurantsByType(chosenType);
            Restaurant restaurant = pickRestaurant(filteredList);

            if (restaurant != null) {
                showRestaurant(restaurant);
            }
        }
    }

    /**
     * Le programme demande les informations nécessaires à l'utilisateur puis crée un nouveau restaurant dans le système.
     */
    private static void addNewRestaurant() {
        System.out.println("Vous allez ajouter un nouveau restaurant !");
        System.out.println("Quel est son nom ?");
        String name = readString();
        System.out.println("Veuillez entrer une courte description : ");
        String description = readString();
        System.out.println("Veuillez entrer l'adresse de son site internet : ");
        String website = readString();
        System.out.println("Rue : ");
        String street = readString();

        City city;
        do {
            city = pickCity(restaurantService.getAllCities());
        } while (city == null);

        RestaurantType restaurantType;
        do {
            restaurantType = pickRestaurantType(restaurantService.getAllRestaurantTypes());
        } while (restaurantType == null);

        Localisation localisation = new Localisation(street, city);
        Restaurant restaurant = new Restaurant(null, name, description, website, localisation, restaurantType);

        restaurant = restaurantService.createRestaurant(restaurant);

        if (restaurant != null) {
            System.out.println("Restaurant créé avec succès !");
            showRestaurant(restaurant);
        } else {
            System.out.println("Erreur lors de la création du restaurant.");
        }
    }

    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis affiche le menu des actions disponibles sur ledit restaurant
     * @param restaurant Le restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        System.out.println("Affichage d'un restaurant : ");
        StringBuilder sb = new StringBuilder();
        sb.append(restaurant.getName()).append("\n");
        sb.append(restaurant.getDescription()).append("\n");
        sb.append(restaurant.getType().getLabel()).append("\n");
        sb.append(restaurant.getWebsite()).append("\n");
        sb.append(restaurant.getAddress().getStreet()).append(", ");
        sb.append(restaurant.getAddress().getCity().getZipCode()).append(" ").append(restaurant.getAddress().getCity().getCityName()).append("\n");
        sb.append("Nombre de likes : ").append(countLikes(restaurant.getEvaluations(), true)).append("\n");
        sb.append("Nombre de dislikes : ").append(countLikes(restaurant.getEvaluations(), false)).append("\n");
        sb.append("\nEvaluations reçues : ").append("\n");

        String text;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            text = getCompleteEvaluationDescription(currentEval);
            if (!text.isEmpty()) {
                sb.append(text).append("\n");
            }
        }

        System.out.println(sb);

        int choice;
        do {
            showRestaurantMenu();
            choice = readInt();
            proceedRestaurantMenu(choice, restaurant);
        } while (choice != 0 && choice != 6);
    }

    /**
     * Parcourt la liste et compte le nombre d'évaluations basiques positives ou négatives en fonction du paramètre likeRestaurant
     * @param evaluations    La liste des évaluations à parcourir
     * @param likeRestaurant Veut-on le nombre d'évaluations positives ou négatives ?
     * @return Le nombre d'évaluations positives ou négatives trouvées
     */
    private static int countLikes(Set<Evaluation> evaluations, Boolean likeRestaurant) {
        int count = 0;
        for (Evaluation currentEval : evaluations) {
            if (currentEval instanceof BasicEvaluation && ((BasicEvaluation) currentEval).getLikeRestaurant() == likeRestaurant) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retourne un String qui contient le détail complet d'une évaluation si elle est de type "CompleteEvaluation". Retourne null s'il s'agit d'une BasicEvaluation
     * @param eval L'évaluation à afficher
     * @return Un String qui contient le détail complet d'une CompleteEvaluation, ou null s'il s'agit d'une BasicEvaluation
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation ce) {
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Éditer ce restaurant");
        System.out.println("5. Éditer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     * @param choice     Un numéro d'action, entre 0 et 6.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        switch (choice) {
            case 1:
                addBasicEvaluation(restaurant, true);
                break;
            case 2:
                addBasicEvaluation(restaurant, false);
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);
                break;
            case 5:
                editRestaurantAddress(restaurant);
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            default:
                break;
        }
    }

    /**
     * Ajoute au restaurant passé en paramètre un like ou un dislike
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retrieve host IP address");
            ipAddress = "Indisponible";
        }

        BasicEvaluation eval = restaurantService.addBasicEvaluation(restaurant, like, ipAddress);

        if (eval != null) {
            restaurant.getEvaluations().add(eval);
            System.out.println("Votre vote a été pris en compte !");
        } else {
            System.out.println("Erreur lors de l'enregistrement du vote.");
        }
    }

    /**
     * Crée une évaluation complète pour le restaurant
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        Set<EvaluationCriteria> criterias = restaurantService.getAllEvaluationCriterias();

        Map<EvaluationCriteria, Integer> grades = new HashMap<>();

        System.out.println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");

        for (EvaluationCriteria currentCriteria : criterias) {
            System.out.println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
            Integer note = readInt();
            grades.put(currentCriteria, note);
        }

        CompleteEvaluation eval = restaurantService.addCompleteEvaluation(restaurant, username, comment, grades);

        if (eval != null) {
            restaurant.getEvaluations().add(eval);
            System.out.println("Votre évaluation a bien été enregistrée, merci !");
        } else {
            System.out.println("Erreur lors de l'enregistrement de l'évaluation.");
        }
    }

    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        System.out.println("Nouveau nom : ");
        restaurant.setName(readString());
        System.out.println("Nouvelle description : ");
        restaurant.setDescription(readString());
        System.out.println("Nouveau site web : ");
        restaurant.setWebsite(readString());
        System.out.println("Nouveau type de restaurant : ");

        RestaurantType newType = pickRestaurantType(restaurantService.getAllRestaurantTypes());
        if (newType != null) {
            restaurant.setType(newType);
        }

        boolean success = restaurantService.updateRestaurant(restaurant);

        if (success) {
            System.out.println("Merci, le restaurant a bien été modifié !");
        } else {
            System.out.println("Erreur lors de la modification du restaurant.");
        }
    }

    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        restaurant.getAddress().setStreet(readString());

        City newCity = pickCity(restaurantService.getAllCities());
        if (newCity != null) {
            restaurant.getAddress().setCity(newCity);
        }

        boolean success = restaurantService.updateRestaurant(restaurant);

        if (success) {
            System.out.println("L'adresse a bien été modifiée ! Merci !");
        } else {
            System.out.println("Erreur lors de la modification de l'adresse.");
        }
    }

    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et toutes ses évaluations
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Êtes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();
        if (choice.equals("o") || choice.equals("O")) {
            boolean success = restaurantService.deleteRestaurant(restaurant);

            if (success) {
                System.out.println("Le restaurant a bien été supprimé !");
            } else {
                System.out.println("Erreur lors de la suppression du restaurant.");
            }
        }
    }

    /**
     * Recherche dans le Set le restaurant comportant le nom passé en paramètre
     */
    private static Restaurant searchRestaurantByName(Set<Restaurant> restaurants, String name) {
        for (Restaurant current : restaurants) {
            if (current.getName().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set la ville comportant le code NPA passé en paramètre
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set le type comportant le libellé passé en paramètre
     */
    private static RestaurantType searchTypeByLabel(Set<RestaurantType> types, String label) {
        for (RestaurantType current : types) {
            if (current.getLabel().equalsIgnoreCase(label)) {
                return current;
            }
        }
        return null;
    }

    /**
     * readInt ne repositionne pas le scanner au début d'une ligne
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do {
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Méthode readString pour rester consistant avec readInt
     */
    private static String readString() {
        return scanner.nextLine();
    }
}