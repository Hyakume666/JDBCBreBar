package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.service.EvaluationService;
import ch.hearc.ig.guideresto.service.RestaurantService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author cedric.baudet
 * @author alain.matile
 * VERSION SERVICE LAYER - Utilise RestaurantService et EvaluationService
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static final RestaurantService restaurantService = RestaurantService.getInstance();
    private static final EvaluationService evaluationService = EvaluationService.getInstance();

    // Constantes pour l'affichage
    private static final String DOUBLE_LINE = "══════════════════════════════════════════════════════════════";
    private static final String SINGLE_LINE = "──────────────────────────────────────────────────────────────";
    private static final String STAR_LINE = "**************************************************************";

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        printWelcomeHeader();

        int choice;
        do {
            printMainMenu();
            choice = readInt();
            proceedMainMenu(choice);
        } while (choice != 0);

        printGoodbyeMessage();

        // Fermer la connexion à la fin
        ConnectionUtils.closeConnection();
    }

    /**
     * Affiche l'en-tête de bienvenue de l'application
     */
    private static void printWelcomeHeader() {
        System.out.println();
        System.out.println(DOUBLE_LINE);
        System.out.println("║                                                            ║");
        System.out.println("║              🍽️  GUIDERESTO - JDBC Edition  🍽️              ║");
        System.out.println("║                                                            ║");
        System.out.println("║           Système de gestion de restaurants                ║");
        System.out.println("║                    Version 1.0.0                           ║");
        System.out.println("║                                                            ║");
        System.out.println(DOUBLE_LINE);
        System.out.println();
    }

    /**
     * Affiche le message d'au revoir
     */
    private static void printGoodbyeMessage() {
        System.out.println();
        System.out.println(STAR_LINE);
        System.out.println("         Merci d'avoir utilisé GuideResto !");
        System.out.println("              À bientôt ! 👋");
        System.out.println(STAR_LINE);
        System.out.println();
    }

    /**
     * Affiche le menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println();
        System.out.println(DOUBLE_LINE);
        System.out.println("                        MENU PRINCIPAL");
        System.out.println(DOUBLE_LINE);
        System.out.println();
        System.out.println("  [1] 📋  Afficher la liste de tous les restaurants");
        System.out.println("  [2] 🔍  Rechercher un restaurant par son nom");
        System.out.println("  [3] 🏙️   Rechercher un restaurant par ville");
        System.out.println("  [4] 🍕  Rechercher un restaurant par son type de cuisine");
        System.out.println("  [5] ➕  Saisir un nouveau restaurant");
        System.out.println();
        System.out.println("  [0] 🚪  Quitter l'application");
        System.out.println();
        System.out.println(SINGLE_LINE);
        System.out.print("Votre choix : ");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice) {
        System.out.println();
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
                // Le message d'au revoir sera affiché après la boucle
                break;
            default:
                printError(Constants.Messages.ERROR_INVALID_INPUT);
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
            printWarning("Aucun restaurant n'a été trouvé !");
            return null;
        }

        printSectionHeader("Liste des restaurants");

        int count = 1;
        for (Restaurant currentRest : restaurants) {
            System.out.printf("  [%d] %s%n", count++, formatRestaurantOneLine(currentRest));
        }

        System.out.println();
        System.out.println(SINGLE_LINE);
        System.out.println("Saisissez le nom exact du restaurant pour voir les détails");
        System.out.println("ou appuyez sur [Entrée] pour revenir en arrière");
        System.out.println(SINGLE_LINE);
        System.out.print("Votre choix : ");
        String choice = readString();

        if (choice.isEmpty()) {
            return null;
        }

        return searchRestaurantByName(restaurants, choice);
    }

    /**
     * Formate un restaurant sur une seule ligne
     */
    private static String formatRestaurantOneLine(Restaurant restaurant) {
        return String.format("\"%s\" - %s, %s %s",
                restaurant.getName(),
                restaurant.getAddress().getStreet(),
                restaurant.getAddress().getCity().getZipCode(),
                restaurant.getAddress().getCity().getCityName());
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList() {
        printSectionHeader("Tous les restaurants");
        Restaurant restaurant = pickRestaurant(restaurantService.getAllRestaurantsWithEvaluations());

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByName() {
        printSectionHeader("Recherche par nom");
        System.out.print("Entrez une partie du nom recherché : ");
        String research = readString();

        if (research.isEmpty()) {
            printWarning("Recherche annulée");
            return;
        }

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
        printSectionHeader("Recherche par ville");
        System.out.print("Entrez une partie du nom de la ville : ");
        String research = readString();

        if (research.isEmpty()) {
            printWarning("Recherche annulée");
            return;
        }

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
        printSectionHeader("Sélection de la ville");

        System.out.println("Villes disponibles :");
        System.out.println();
        for (City currentCity : cities) {
            System.out.printf("  • %s - %s%n", currentCity.getZipCode(), currentCity.getCityName());
        }

        System.out.println();
        System.out.println(SINGLE_LINE);
        System.out.println("Entrez le NPA de la ville désirée");
        System.out.println("ou tapez \"NEW\" pour créer une nouvelle ville");
        System.out.println(SINGLE_LINE);
        System.out.print("Votre choix : ");
        String choice = readString();

        if (choice.equalsIgnoreCase("NEW")) {
            City city = new City();
            System.out.print("\nNPA de la nouvelle ville : ");
            city.setZipCode(readString());
            System.out.print("Nom de la nouvelle ville : ");
            city.setCityName(readString());

            city = restaurantService.createCity(city);

            if (city != null) {
                printSuccess("Ville créée avec succès !");
            }

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
        printSectionHeader("Sélection du type de cuisine");

        System.out.println("Types disponibles :");
        System.out.println();
        for (RestaurantType currentType : types) {
            System.out.printf("  • \"%s\" - %s%n", currentType.getLabel(), currentType.getDescription());
        }

        System.out.println();
        System.out.println(SINGLE_LINE);
        System.out.print("Entrez le libellé exact du type désiré : ");
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
        printSectionHeader("Création d'un nouveau restaurant");

        System.out.print("Nom du restaurant : ");
        String name = readString();
        System.out.print("Description : ");
        String description = readString();
        System.out.print("Site internet : ");
        String website = readString();
        System.out.print("Rue : ");
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
            printSuccess("Restaurant créé avec succès !");
            System.out.println();
            showRestaurant(restaurant);
        } else {
            printError("Erreur lors de la création du restaurant.");
        }
    }

    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis affiche le menu des actions disponibles sur ledit restaurant
     * @param restaurant Le restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        System.out.println();
        System.out.println(DOUBLE_LINE);
        System.out.printf("                    %s%n", restaurant.getName().toUpperCase());
        System.out.println(DOUBLE_LINE);
        System.out.println();

        // Informations générales
        System.out.println("📍 INFORMATIONS");
        System.out.println(SINGLE_LINE);
        System.out.printf("   Type de cuisine : %s%n", restaurant.getType().getLabel());
        System.out.printf("   Adresse         : %s%n", restaurant.getAddress().getStreet());
        System.out.printf("                     %s %s%n",
                restaurant.getAddress().getCity().getZipCode(),
                restaurant.getAddress().getCity().getCityName());
        if (restaurant.getWebsite() != null && !restaurant.getWebsite().isEmpty()) {
            System.out.printf("   Site web        : %s%n", restaurant.getWebsite());
        }
        System.out.println();

        if (restaurant.getDescription() != null && !restaurant.getDescription().isEmpty()) {
            System.out.println("📝 DESCRIPTION");
            System.out.println(SINGLE_LINE);
            System.out.printf("   %s%n", restaurant.getDescription());
            System.out.println();
        }

        // Statistiques
        System.out.println("📊 STATISTIQUES");
        System.out.println(SINGLE_LINE);
        int likes = countLikes(restaurant.getEvaluations(), true);
        int dislikes = countLikes(restaurant.getEvaluations(), false);
        System.out.printf("   👍 Likes    : %d%n", likes);
        System.out.printf("   👎 Dislikes : %d%n", dislikes);
        System.out.println();

        // Évaluations détaillées
        List<CompleteEvaluation> completeEvals = restaurant.getEvaluations().stream()
                .filter(e -> e instanceof CompleteEvaluation)
                .map(e -> (CompleteEvaluation) e)
                .toList();

        if (!completeEvals.isEmpty()) {
            System.out.println("💬 ÉVALUATIONS DÉTAILLÉES");
            System.out.println(SINGLE_LINE);
            for (CompleteEvaluation eval : completeEvals) {
                System.out.printf("%n   👤 %s%n", eval.getUsername());
                System.out.printf("   💭 \"%s\"%n", eval.getComment());
                System.out.println("   Notes :");
                for (Grade grade : eval.getGrades()) {
                    String stars = "★".repeat(grade.getGrade()) + "☆".repeat(5 - grade.getGrade());
                    System.out.printf("      • %s : %s (%d/5)%n",
                            grade.getCriteria().getName(),
                            stars,
                            grade.getGrade());
                }
                System.out.println();
            }
        }

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
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println(DOUBLE_LINE);
        System.out.println("                    ACTIONS DISPONIBLES");
        System.out.println(DOUBLE_LINE);
        System.out.println();
        System.out.println("  [1] 👍  J'aime ce restaurant !");
        System.out.println("  [2] 👎  Je n'aime pas ce restaurant !");
        System.out.println("  [3] ⭐  Faire une évaluation complète");
        System.out.println("  [4] ✏️   Éditer ce restaurant");
        System.out.println("  [5] 📍  Éditer l'adresse du restaurant");
        System.out.println("  [6] 🗑️   Supprimer ce restaurant");
        System.out.println();
        System.out.println("  [0] ⬅️   Revenir au menu principal");
        System.out.println();
        System.out.println(SINGLE_LINE);
        System.out.print("Votre choix : ");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     * @param choice     Un numéro d'action, entre 0 et 6.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        System.out.println();
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
            ipAddress = Constants.Evaluation.IP_UNAVAILABLE;
        }

        BasicEvaluation eval = evaluationService.addBasicEvaluation(restaurant, like, ipAddress);

        if (eval != null) {
            restaurant.getEvaluations().add(eval);
            printSuccess(Constants.Messages.SUCCESS_VOTE_RECORDED);
        } else {
            printError(Constants.Messages.ERROR_VOTE_FAILED);
        }
    }

    /**
     * Crée une évaluation complète pour le restaurant avec validation des notes
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        printSectionHeader("Évaluation complète du restaurant");

        System.out.print("Votre nom d'utilisateur : ");
        String username = readString();
        System.out.print("Votre commentaire : ");
        String comment = readString();

        Set<EvaluationCriteria> criterias = evaluationService.getAllEvaluationCriterias();
        Map<EvaluationCriteria, Integer> grades = new HashMap<>();

        System.out.println();
        System.out.println(String.format(Constants.Messages.PROMPT_GRADE_RANGE,
                Constants.Evaluation.MIN_GRADE,
                Constants.Evaluation.MAX_GRADE));
        System.out.println();

        for (EvaluationCriteria currentCriteria : criterias) {
            System.out.printf("🔹 %s%n", currentCriteria.getName());
            System.out.printf("   %s%n", currentCriteria.getDescription());
            System.out.print("   Votre note : ");

            Integer note;
            do {
                note = readInt();
                if (note < Constants.Evaluation.MIN_GRADE || note > Constants.Evaluation.MAX_GRADE) {
                    System.out.println(String.format("   ❌ Note invalide ! Veuillez entrer une note entre %d et %d :",
                            Constants.Evaluation.MIN_GRADE,
                            Constants.Evaluation.MAX_GRADE));
                    System.out.print("   Votre note : ");
                }
            } while (note < Constants.Evaluation.MIN_GRADE || note > Constants.Evaluation.MAX_GRADE);

            grades.put(currentCriteria, note);
            System.out.println();
        }

        CompleteEvaluation eval = evaluationService.addCompleteEvaluation(restaurant, username, comment, grades);

        if (eval != null) {
            restaurant.getEvaluations().add(eval);
            printSuccess(Constants.Messages.SUCCESS_EVALUATION_RECORDED);
        } else {
            printError(Constants.Messages.ERROR_EVALUATION_FAILED);
        }
    }

    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant
     */
    private static void editRestaurant(Restaurant restaurant) {
        printSectionHeader("Édition du restaurant");

        System.out.print("Nouveau nom : ");
        restaurant.setName(readString());
        System.out.print("Nouvelle description : ");
        restaurant.setDescription(readString());
        System.out.print("Nouveau site web : ");
        restaurant.setWebsite(readString());

        System.out.println();
        RestaurantType newType = pickRestaurantType(restaurantService.getAllRestaurantTypes());
        if (newType != null) {
            restaurant.setType(newType);
        }

        boolean success = restaurantService.updateRestaurant(restaurant);

        if (success) {
            printSuccess("Le restaurant a bien été modifié !");
        } else {
            printError("Erreur lors de la modification du restaurant.");
        }
    }

    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        printSectionHeader("Édition de l'adresse");

        System.out.print("Nouvelle rue : ");
        restaurant.getAddress().setStreet(readString());

        System.out.println();
        City newCity = pickCity(restaurantService.getAllCities());
        if (newCity != null) {
            restaurant.getAddress().setCity(newCity);
        }

        boolean success = restaurantService.updateRestaurant(restaurant);

        if (success) {
            printSuccess("L'adresse a bien été modifiée !");
        } else {
            printError("Erreur lors de la modification de l'adresse.");
        }
    }

    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et toutes ses évaluations
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        printWarning("⚠️  ATTENTION : Cette action est irréversible !");
        System.out.println();
        System.out.print("Êtes-vous sûr de vouloir supprimer ce restaurant ? (O/n) : ");
        String choice = readString();

        if (choice.equalsIgnoreCase("o") || choice.equalsIgnoreCase("O")) {
            boolean success = restaurantService.deleteRestaurant(restaurant);

            if (success) {
                printSuccess("Le restaurant a bien été supprimé !");
            } else {
                printError("Erreur lors de la suppression du restaurant.");
            }
        } else {
            System.out.println("Suppression annulée.");
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
        printWarning("Aucun restaurant trouvé avec ce nom.");
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
        printWarning("Aucune ville trouvée avec ce NPA.");
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
        printWarning("Aucun type trouvé avec ce libellé.");
        return null;
    }

    // ========================================================================
    // MÉTHODES UTILITAIRES POUR L'AFFICHAGE
    // ========================================================================

    /**
     * Affiche un en-tête de section
     */
    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(DOUBLE_LINE);
        System.out.printf("  %s%n", title.toUpperCase());
        System.out.println(DOUBLE_LINE);
        System.out.println();
    }

    /**
     * Affiche un message de succès
     */
    private static void printSuccess(String message) {
        System.out.println();
        System.out.println("✅ " + message);
        System.out.println();
    }

    /**
     * Affiche un message d'erreur
     */
    private static void printError(String message) {
        System.out.println();
        System.out.println("❌ " + message);
        System.out.println();
    }

    /**
     * Affiche un message d'avertissement
     */
    private static void printWarning(String message) {
        System.out.println();
        System.out.println("⚠️  " + message);
        System.out.println();
    }

    // ========================================================================
    // MÉTHODES DE LECTURE UTILISATEUR
    // ========================================================================

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
                printError(Constants.Messages.ERROR_MUST_BE_INTEGER);
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