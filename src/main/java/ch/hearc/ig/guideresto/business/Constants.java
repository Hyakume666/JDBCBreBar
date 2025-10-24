package ch.hearc.ig.guideresto.business;

/**
 * Classe contenant les constantes métier de l'application.
 * Centralise les valeurs pour faciliter la maintenance.
 */
public final class Constants {

    // Empêche l'instanciation
    private Constants() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }

    /**
     * Constantes liées aux évaluations
     */
    public static final class Evaluation {
        public static final int MIN_GRADE = 1;
        public static final int MAX_GRADE = 5;
        public static final String IP_UNAVAILABLE = "Indisponible";
    }

    /**
     * Constantes liées aux messages utilisateur
     */
    public static final class Messages {
        public static final String ERROR_INVALID_INPUT = "Erreur : saisie incorrecte. Veuillez réessayer";
        public static final String ERROR_MUST_BE_INTEGER = "Erreur ! Veuillez entrer un nombre entier s'il vous plaît !";
        public static final String SUCCESS_VOTE_RECORDED = "Votre vote a été pris en compte !";
        public static final String SUCCESS_EVALUATION_RECORDED = "Votre évaluation a bien été enregistrée, merci !";
        public static final String ERROR_VOTE_FAILED = "Erreur lors de l'enregistrement du vote.";
        public static final String ERROR_EVALUATION_FAILED = "Erreur lors de l'enregistrement de l'évaluation.";
        public static final String PROMPT_GRADE_RANGE = "Veuillez svp donner une note entre %d et %d pour chacun de ces critères : ";
    }
}