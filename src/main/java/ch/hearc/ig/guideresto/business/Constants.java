package ch.hearc.ig.guideresto.business;

/**
 * Classe contenant les constantes métier de l'application.
 * Centralise toutes les valeurs constantes pour faciliter la maintenance et respecter le principe DRY.
 *
 * <p>Cette classe utilise le pattern <b>Constants Class</b> avec des classes internes statiques
 * pour organiser les constantes par domaine fonctionnel.</p>
 *
 * <p><b>Avantages de cette approche :</b></p>
 * <ul>
 *   <li><b>DRY (Don't Repeat Yourself) :</b> Une seule source de vérité</li>
 *   <li><b>Maintenance facilitée :</b> Modifier une valeur = un seul endroit</li>
 *   <li><b>Typage fort :</b> Constantes typées, pas de magic numbers</li>
 *   <li><b>Organisation claire :</b> Regroupement par domaine</li>
 *   <li><b>Documentation centralisée :</b> Toutes les règles métier au même endroit</li>
 * </ul>
 *
 * <p><b>Organisation des constantes :</b></p>
 * <pre>
 * Constants (classe racine)
 *     ├── Evaluation (constantes liées aux évaluations)
 *     ├── UI (constantes pour l'interface utilisateur)
 *     └── Messages (messages utilisateur)
 * </pre>
 *
 * <p><b>Exemple d'utilisation :</b></p>
 * <pre>
 * // Validation d'une note
 * if (note &lt; Constants.Evaluation.MIN_GRADE ||
 *     note &gt; Constants.Evaluation.MAX_GRADE) {
 *     throw new IllegalArgumentException("Note invalide");
 * }
 *
 * // Affichage d'un message
 * System.out.println(Constants.Messages.SUCCESS_VOTE_RECORDED);
 *
 * // Utilisation d'un séparateur
 * System.out.println(Constants.UI.DOUBLE_LINE);
 * </pre>
 *
 * <p><b>Bonnes pratiques appliquées :</b></p>
 * <ul>
 *   <li>Classe finale (ne peut pas être héritée)</li>
 *   <li>Constructeur privé (ne peut pas être instanciée)</li>
 *   <li>Toutes les constantes sont public static final</li>
 *   <li>Nommage en SCREAMING_SNAKE_CASE</li>
 *   <li>Classes internes statiques pour l'organisation</li>
 * </ul>
 *
 * @author Votre Nom
 * @version 2.0
 * @since 1.0
 */
public final class Constants {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     *
     * <p>Cette classe est purement utilitaire et ne doit jamais être instanciée.
     * Le constructeur privé garantit cette contrainte au niveau du compilateur.</p>
     *
     * <p>Si quelqu'un tente d'instancier via réflexion, une AssertionError sera levée.</p>
     *
     * @throws AssertionError si quelqu'un tente d'instancier cette classe
     */
    private Constants() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }

    /**
     * Constantes liées aux évaluations de restaurants.
     * Définit les règles métier pour les notes et les évaluations.
     *
     * <p><b>Règles métier :</b></p>
     * <ul>
     *   <li>Les notes doivent être comprises entre MIN_GRADE et MAX_GRADE (1-5)</li>
     *   <li>Si l'adresse IP est indisponible, utiliser IP_UNAVAILABLE</li>
     * </ul>
     *
     * @see ch.hearc.ig.guideresto.business.Grade
     * @see ch.hearc.ig.guideresto.business.BasicEvaluation
     * @see ch.hearc.ig.guideresto.service.EvaluationService
     */
    public static final class Evaluation {

        /**
         * Note minimale autorisée pour une évaluation.
         *
         * <p>Toute note inférieure à cette valeur est considérée comme invalide.</p>
         *
         * <p><b>Valeur :</b> 1 (sur une échelle de 1 à 5)</p>
         */
        public static final int MIN_GRADE = 1;

        /**
         * Note maximale autorisée pour une évaluation.
         *
         * <p>Toute note supérieure à cette valeur est considérée comme invalide.</p>
         *
         * <p><b>Valeur :</b> 5 (sur une échelle de 1 à 5)</p>
         */
        public static final int MAX_GRADE = 5;

        /**
         * Valeur par défaut pour l'adresse IP lorsqu'elle ne peut pas être déterminée.
         *
         * <p>Cette constante est utilisée dans les évaluations basiques lorsque
         * la récupération de l'adresse IP échoue.</p>
         *
         * <p><b>Valeur :</b> "Indisponible"</p>
         */
        public static final String IP_UNAVAILABLE = "Indisponible";
    }

    /**
     * Constantes pour l'interface utilisateur (séparateurs, symboles Unicode).
     * Centralise tous les éléments visuels de l'application console.
     *
     * <p><b>Avantages de centraliser les éléments UI :</b></p>
     * <ul>
     *   <li>Cohérence visuelle dans toute l'application</li>
     *   <li>Modification facile du thème visuel</li>
     *   <li>Réutilisabilité des éléments graphiques</li>
     * </ul>
     *
     * <p><b>Exemple d'utilisation :</b></p>
     * <pre>
     * System.out.println(Constants.UI.DOUBLE_LINE);
     * System.out.println("       MENU PRINCIPAL");
     * System.out.println(Constants.UI.DOUBLE_LINE);
     * </pre>
     *
     * @see ch.hearc.ig.guideresto.presentation.Application
     */
    public static final class UI {

        /**
         * Ligne de séparation double pour les en-têtes principaux.
         * Utilisée pour les titres de sections importantes.
         *
         * <p><b>Longueur :</b> 62 caractères</p>
         */
        public static final String DOUBLE_LINE = "══════════════════════════════════════════════════════════════";

        /**
         * Ligne de séparation simple pour les sous-sections.
         * Utilisée pour séparer visuellement des blocs de contenu.
         *
         * <p><b>Longueur :</b> 62 caractères</p>
         */
        public static final String SINGLE_LINE = "──────────────────────────────────────────────────────────────";

        /**
         * Ligne d'étoiles pour les messages spéciaux (bienvenue, au revoir).
         * Utilisée pour attirer l'attention sur des messages importants.
         *
         * <p><b>Longueur :</b> 62 caractères</p>
         */
        public static final String STAR_LINE = "**************************************************************";

        /**
         * Symbole Unicode pour représenter un restaurant.
         * Utilisé dans les en-têtes et titres.
         */
        public static final String EMOJI_RESTAURANT = "🍽️";

        /**
         * Symbole Unicode pour indiquer une action réussie.
         * Utilisé dans les messages de confirmation.
         */
        public static final String EMOJI_CHECK = "✅";

        /**
         * Symbole Unicode pour indiquer une erreur.
         * Utilisé dans les messages d'erreur.
         */
        public static final String EMOJI_ERROR = "❌";

        /**
         * Symbole Unicode pour indiquer un avertissement.
         * Utilisé dans les messages d'avertissement.
         */
        public static final String EMOJI_WARNING = "⚠️";

        /**
         * Symbole Unicode pour un "like" (pouce levé).
         * Utilisé pour les évaluations positives.
         */
        public static final String EMOJI_LIKE = "👍";

        /**
         * Symbole Unicode pour un "dislike" (pouce baissé).
         * Utilisé pour les évaluations négatives.
         */
        public static final String EMOJI_DISLIKE = "👎";
    }

    /**
     * Messages utilisateur standardisés de l'application.
     * Centralise tous les messages affichés à l'utilisateur pour faciliter
     * la maintenance et l'internationalisation future.
     *
     * <p><b>Types de messages :</b></p>
     * <ul>
     *   <li><b>Messages d'erreur :</b> Préfixe ERROR_*</li>
     *   <li><b>Messages de succès :</b> Préfixe SUCCESS_*</li>
     *   <li><b>Messages informatifs :</b> Préfixe INFO_* ou PROMPT_*</li>
     * </ul>
     *
     * <p><b>Avantages de centraliser les messages :</b></p>
     * <ul>
     *   <li><b>Cohérence :</b> Tous les messages au même endroit</li>
     *   <li><b>Maintenance :</b> Modifier un message = un seul endroit</li>
     *   <li><b>Internationalisation :</b> Facilite la traduction future</li>
     *   <li><b>Réutilisabilité :</b> Même message utilisable partout</li>
     * </ul>
     *
     * @see ch.hearc.ig.guideresto.presentation.Application
     */
    public static final class Messages {

        // ========== Messages d'erreur ==========

        /**
         * Message d'erreur générique pour une saisie incorrecte.
         */
        public static final String ERROR_INVALID_INPUT = "Erreur : saisie incorrecte. Veuillez réessayer";

        /**
         * Message d'erreur spécifique lorsqu'un entier est attendu mais non fourni.
         */
        public static final String ERROR_MUST_BE_INTEGER = "Erreur ! Veuillez entrer un nombre entier s'il vous plaît !";

        /**
         * Message d'erreur lors de l'échec de l'enregistrement d'un vote.
         */
        public static final String ERROR_VOTE_FAILED = "Erreur lors de l'enregistrement du vote.";

        /**
         * Message d'erreur lors de l'échec de l'enregistrement d'une évaluation complète.
         */
        public static final String ERROR_EVALUATION_FAILED = "Erreur lors de l'enregistrement de l'évaluation.";

        /**
         * Message d'erreur lors de l'échec de création d'un restaurant.
         */
        public static final String ERROR_RESTAURANT_CREATE_FAILED = "Erreur lors de la création du restaurant.";

        /**
         * Message d'erreur lors de l'échec de modification d'un restaurant.
         */
        public static final String ERROR_RESTAURANT_UPDATE_FAILED = "Erreur lors de la modification du restaurant.";

        /**
         * Message d'erreur lors de l'échec de suppression d'un restaurant.
         */
        public static final String ERROR_RESTAURANT_DELETE_FAILED = "Erreur lors de la suppression du restaurant.";

        /**
         * Message d'erreur lors de l'échec de modification d'une adresse.
         */
        public static final String ERROR_ADDRESS_UPDATE_FAILED = "Erreur lors de la modification de l'adresse.";

        /**
         * Message affiché lorsqu'une note est invalide (hors de la plage MIN_GRADE - MAX_GRADE).
         * Contient des placeholders pour MIN_GRADE et MAX_GRADE.
         */
        public static final String ERROR_INVALID_GRADE = "❌ Note invalide ! Veuillez entrer une note entre %d et %d :";

        // ========== Messages de succès ==========

        /**
         * Message de succès après l'enregistrement d'un vote (like/dislike).
         */
        public static final String SUCCESS_VOTE_RECORDED = "Votre vote a été pris en compte !";

        /**
         * Message de succès après l'enregistrement d'une évaluation complète.
         */
        public static final String SUCCESS_EVALUATION_RECORDED = "Votre évaluation a bien été enregistrée, merci !";

        /**
         * Message de succès après la création d'un restaurant.
         */
        public static final String SUCCESS_RESTAURANT_CREATED = "Restaurant créé avec succès !";

        /**
         * Message de succès après la modification d'un restaurant.
         */
        public static final String SUCCESS_RESTAURANT_UPDATED = "Le restaurant a bien été modifié !";

        /**
         * Message de succès après la suppression d'un restaurant.
         */
        public static final String SUCCESS_RESTAURANT_DELETED = "Le restaurant a bien été supprimé !";

        /**
         * Message de succès après la modification d'une adresse.
         */
        public static final String SUCCESS_ADDRESS_UPDATED = "L'adresse a bien été modifiée !";

        /**
         * Message de succès après la création d'une ville.
         */
        public static final String SUCCESS_CITY_CREATED = "Ville créée avec succès !";

        // ========== Messages informatifs ==========

        /**
         * Message affiché quand aucun restaurant n'est trouvé.
         */
        public static final String INFO_NO_RESTAURANT_FOUND = "Aucun restaurant n'a été trouvé !";

        /**
         * Message affiché quand une recherche est annulée.
         */
        public static final String INFO_SEARCH_CANCELLED = "Recherche annulée";

        /**
         * Message affiché quand une suppression est annulée.
         */
        public static final String INFO_DELETE_CANCELLED = "Suppression annulée.";

        /**
         * Message affiché quand aucun restaurant ne correspond au nom saisi.
         */
        public static final String INFO_NO_RESTAURANT_WITH_NAME = "Aucun restaurant trouvé avec ce nom.";

        /**
         * Message affiché quand aucune ville ne correspond au NPA saisi.
         */
        public static final String INFO_NO_CITY_WITH_ZIPCODE = "Aucune ville trouvée avec ce NPA.";

        /**
         * Message affiché quand aucun type ne correspond au libellé saisi.
         */
        public static final String INFO_NO_TYPE_WITH_LABEL = "Aucun type trouvé avec ce libellé.";

        // ========== Messages de prompt ==========

        /**
         * Message de prompt demandant à l'utilisateur de noter selon l'échelle définie.
         * Contient des placeholders pour MIN_GRADE et MAX_GRADE.
         */
        public static final String PROMPT_GRADE_RANGE = "Veuillez svp donner une note entre %d et %d pour chacun de ces critères : ";

        /**
         * Message demandant à l'utilisateur d'entrer une partie du nom recherché.
         */
        public static final String PROMPT_ENTER_NAME_PART = "Entrez une partie du nom recherché : ";

        /**
         * Message demandant à l'utilisateur d'entrer une partie du nom de ville.
         */
        public static final String PROMPT_ENTER_CITY_PART = "Entrez une partie du nom de la ville : ";

        /**
         * Message de confirmation avant suppression d'un restaurant.
         */
        public static final String PROMPT_DELETE_CONFIRM = "Êtes-vous sûr de vouloir supprimer ce restaurant ? (O/n) : ";

        // ========== Messages d'avertissement ==========

        /**
         * Avertissement avant une action irréversible (suppression).
         */
        public static final String WARNING_IRREVERSIBLE_ACTION = "⚠️  ATTENTION : Cette action est irréversible !";
    }
}