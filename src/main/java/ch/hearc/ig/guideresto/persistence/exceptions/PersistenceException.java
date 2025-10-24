package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception de base pour toutes les erreurs de persistance.
 * Permet une gestion centralisée des erreurs liées à la base de données.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}