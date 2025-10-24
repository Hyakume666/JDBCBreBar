package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception levée lors d'une erreur SQL (INSERT, UPDATE, DELETE).
 */
public class DatabaseOperationException extends PersistenceException {

    private final String operation;

    public DatabaseOperationException(String operation, Throwable cause) {
        super(String.format("Erreur lors de l'opération %s", operation), cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}