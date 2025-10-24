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
 * // Récupération de l'IP par défaut
 * String ip = getIpAddress().orElse(Constants.Evaluation.IP_UNAVAILABLE);
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
 * <p><b>Extensions futures possibles :</b></p>
 * <pre>
 * public static final class Validation {
 *     public static final int MIN_NAME_LENGTH = 2;
 *     public static final int MAX_NAME_LENGTH = 100;
 *     public static final String ZIP_CODE_PATTERN = "^[0-9]{4}$";
 * }
 *
 * public static final class Database {
 *     public static final int CONNECTION_TIMEOUT = 30;
 *     public static final int MAX_RETRY_ATTEMPTS = 3;
 * }
 *
 * public static final class UI {
 *     public static final String DOUBLE_LINE = "══════...";
 *     public static final String SINGLE_LINE = "──────...";
 * }
 * </pre>
 *
 * <p><b>Pattern utilisé :</b> Constants Class avec Inner Classes</p>
 *
 * <p><b>Alternative considérée :</b> Utiliser des enums pour certaines constantes
 * (par exemple pour les types d'évaluations), mais les constantes primitives
 * sont plus simples et suffisantes pour ce projet.</p>
 *
 * @author Votre Nom
 * @version 1.0
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
     * <p><b>Exemples d'utilisation :</b></p>
     * <pre>
     * // Validation d'une note
     * public void validateGrade(int grade) {
     *     if (grade &lt; Evaluation.MIN_GRADE || grade &gt; Evaluation.MAX_GRADE) {
     *         throw new IllegalArgumentException(
     *             String.format("La note doit être entre %d et %d",
     *                 Evaluation.MIN_GRADE, Evaluation.MAX_GRADE)
     *         );
     *     }
     * }
     *
     * // Affichage d'un prompt
     * System.out.printf("Donnez une note entre %d et %d : ",
     *                   Evaluation.MIN_GRADE, Evaluation.MAX_GRADE);
     *
     * // Gestion de l'IP indisponible
     * String ip;
     * try {
     *     ip = InetAddress.getLocalHost().getHostAddress();
     * } catch (UnknownHostException ex) {
     *     ip = Evaluation.IP_UNAVAILABLE;
     * }
     * </pre>
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
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Validation des notes dans EvaluationService</li>
         *   <li>Validation des notes dans Validator (si implémenté)</li>
         *   <li>Messages d'erreur pour l'utilisateur</li>
         * </ul>
         */
        public static final int MIN_GRADE = 1;

        /**
         * Note maximale autorisée pour une évaluation.
         *
         * <p>Toute note supérieure à cette valeur est considérée comme invalide.</p>
         *
         * <p><b>Valeur :</b> 5 (sur une échelle de 1 à 5)</p>
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Validation des notes dans EvaluationService</li>
         *   <li>Validation des notes dans Validator (si implémenté)</li>
         *   <li>Messages d'erreur pour l'utilisateur</li>
         * </ul>
         *
         * <p><b>Note :</b> Si l'échelle de notation change (par exemple 1-10),
         * il suffit de modifier cette constante.</p>
         */
        public static final int MAX_GRADE = 5;

        /**
         * Valeur par défaut pour l'adresse IP lorsqu'elle ne peut pas être déterminée.
         *
         * <p>Cette constante est utilisée dans les évaluations basiques lorsque
         * la récupération de l'adresse IP échoue (typiquement lors d'une
         * {@link java.net.UnknownHostException}).</p>
         *
         * <p><b>Valeur :</b> "Indisponible"</p>
         *
         * <p><b>Cas d'utilisation :</b></p>
         * <pre>
         * String ipAddress;
         * try {
         *     ipAddress = Inet4Address.getLocalHost().toString();
         * } catch (UnknownHostException ex) {
         *     logger.error("Impossible de récupérer l'adresse IP");
         *     ipAddress = Constants.Evaluation.IP_UNAVAILABLE;
         * }
         * </pre>
         *
         * <p><b>Note :</b> Cette valeur sera stockée en base de données dans
         * la table LIKES si l'IP ne peut pas être déterminée.</p>
         *
         * @see ch.hearc.ig.guideresto.business.BasicEvaluation#getIpAddress()
         */
        public static final String IP_UNAVAILABLE = "Indisponible";
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
     *   <li><b>Messages informatifs :</b> Préfixe PROMPT_* ou INFO_*</li>
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
     * <p><b>Exemple d'utilisation :</b></p>
     * <pre>
     * // Affichage d'un message de succès
     * if (evaluationCreated) {
     *     System.out.println("✅ " + Messages.SUCCESS_EVALUATION_RECORDED);
     * }
     *
     * // Affichage d'un message d'erreur
     * try {
     *     int number = scanner.nextInt();
     * } catch (InputMismatchException e) {
     *     System.out.println("❌ " + Messages.ERROR_MUST_BE_INTEGER);
     * }
     *
     * // Message formaté avec paramètres
     * System.out.printf(Messages.PROMPT_GRADE_RANGE,
     *                   Evaluation.MIN_GRADE,
     *                   Evaluation.MAX_GRADE);
     * </pre>
     *
     * <p><b>Extension future pour l'internationalisation :</b></p>
     * <pre>
     * // Utiliser ResourceBundle pour les traductions
     * ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
     * String message = messages.getString("error.invalid.input");
     * </pre>
     *
     * @see ch.hearc.ig.guideresto.presentation.Application
     */
    public static final class Messages {

        /**
         * Message d'erreur générique pour une saisie incorrecte.
         * Utilisé lorsque l'utilisateur entre une valeur invalide (format, type, etc.).
         *
         * <p><b>Valeur :</b> "Erreur : saisie incorrecte. Veuillez réessayer"</p>
         *
         * <p><b>Cas d'utilisation :</b> Validation générale des entrées utilisateur</p>
         */
        public static final String ERROR_INVALID_INPUT = "Erreur : saisie incorrecte. Veuillez réessayer";

        /**
         * Message d'erreur spécifique lorsqu'un entier est attendu mais non fourni.
         *
         * <p><b>Valeur :</b> "Erreur ! Veuillez entrer un nombre entier s'il vous plaît !"</p>
         *
         * <p><b>Cas d'utilisation typique :</b></p>
         * <pre>
         * try {
         *     int choice = scanner.nextInt();
         * } catch (InputMismatchException e) {
         *     System.out.println(Messages.ERROR_MUST_BE_INTEGER);
         *     scanner.nextLine(); // Clear buffer
         * }
         * </pre>
         */
        public static final String ERROR_MUST_BE_INTEGER = "Erreur ! Veuillez entrer un nombre entier s'il vous plaît !";

        /**
         * Message de succès après l'enregistrement d'un vote (like/dislike).
         *
         * <p><b>Valeur :</b> "Votre vote a été pris en compte !"</p>
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Application.addBasicEvaluation() après un vote réussi</li>
         * </ul>
         */
        public static final String SUCCESS_VOTE_RECORDED = "Votre vote a été pris en compte !";

        /**
         * Message de succès après l'enregistrement d'une évaluation complète.
         *
         * <p><b>Valeur :</b> "Votre évaluation a bien été enregistrée, merci !"</p>
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Application.evaluateRestaurant() après une évaluation complète réussie</li>
         * </ul>
         */
        public static final String SUCCESS_EVALUATION_RECORDED = "Votre évaluation a bien été enregistrée, merci !";

        /**
         * Message d'erreur lors de l'échec de l'enregistrement d'un vote.
         *
         * <p><b>Valeur :</b> "Erreur lors de l'enregistrement du vote."</p>
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Application.addBasicEvaluation() en cas d'échec</li>
         * </ul>
         */
        public static final String ERROR_VOTE_FAILED = "Erreur lors de l'enregistrement du vote.";

        /**
         * Message d'erreur lors de l'échec de l'enregistrement d'une évaluation complète.
         *
         * <p><b>Valeur :</b> "Erreur lors de l'enregistrement de l'évaluation."</p>
         *
         * <p><b>Utilisé dans :</b></p>
         * <ul>
         *   <li>Application.evaluateRestaurant() en cas d'échec</li>
         * </ul>
         */
        public static final String ERROR_EVALUATION_FAILED = "Erreur lors de l'enregistrement de l'évaluation.";

        /**
         * Message de prompt demandant à l'utilisateur de noter selon l'échelle définie.
         * Contient des placeholders pour MIN_GRADE et MAX_GRADE.
         *
         * <p><b>Valeur :</b> "Veuillez svp donner une note entre %d et %d pour chacun de ces critères : "</p>
         *
         * <p><b>Utilisation avec String.format() :</b></p>
         * <pre>
         * System.out.printf(Messages.PROMPT_GRADE_RANGE,
         *                   Evaluation.MIN_GRADE,    // %d → 1
         *                   Evaluation.MAX_GRADE);   // %d → 5
         *
         * // Affiche : "Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : "
         * </pre>
         *
         * <p><b>Note :</b> Les valeurs MIN_GRADE et MAX_GRADE proviennent de
         * {@link Evaluation#MIN_GRADE} et {@link Evaluation#MAX_GRADE}.</p>
         */
        public static final String PROMPT_GRADE_RANGE = "Veuillez svp donner une note entre %d et %d pour chacun de ces critères : ";
    }
}