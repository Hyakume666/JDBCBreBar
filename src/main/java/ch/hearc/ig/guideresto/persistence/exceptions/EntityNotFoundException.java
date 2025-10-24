package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception levée lorsqu'une entité demandée n'existe pas en base.
 */
public class EntityNotFoundException extends PersistenceException {

    private final Integer entityId;
    private final String entityType;

    public EntityNotFoundException(String entityType, Integer entityId) {
        super(String.format("%s avec l'ID %d introuvable", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }
}