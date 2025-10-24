package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception de base pour toutes les erreurs de persistance.
 * Permet une gestion centralisée des erreurs liées à la base de données.
 *
 * <p>Cette classe constitue la racine de la hiérarchie d'exceptions personnalisées
 * du système de persistance. Elle hérite de {@link RuntimeException} pour permettre
 * une gestion flexible des erreurs sans obligation de déclaration dans les signatures
 * de méthodes.</p>
 *
 * <p><b>Hiérarchie des exceptions :</b></p>
 * <pre>
 * PersistenceException (classe abstraite de base)
 *     ├── DatabaseOperationException (erreurs SQL)
 *     └── EntityNotFoundException (entité introuvable)
 * </pre>
 *
 * <p><b>Types d'erreurs couvertes :</b></p>
 * <ul>
 *   <li>Erreurs de connexion à la base de données</li>
 *   <li>Erreurs SQL (INSERT, UPDATE, DELETE, SELECT)</li>
 *   <li>Violations de contraintes d'intégrité</li>
 *   <li>Entités non trouvées</li>
 *   <li>Erreurs de mapping objet-relationnel</li>
 *   <li>Problèmes transactionnels (commit/rollback)</li>
 * </ul>
 *
 * <p><b>Avantages de cette approche :</b></p>
 * <ul>
 *   <li><b>Centralisation :</b> Toutes les erreurs de persistance héritent de cette classe</li>
 *   <li><b>Traçabilité :</b> Facilite le logging et le debugging</li>
 *   <li><b>Flexibilité :</b> RuntimeException = pas d'obligation de try-catch partout</li>
 *   <li><b>Maintenance :</b> Ajouter un nouveau type d'exception = hériter de cette classe</li>
 * </ul>
 *
 * <p><b>Exemple d'utilisation typique :</b></p>
 * <pre>
 * try {
 *     Restaurant restaurant = restaurantMapper.findById(1);
 * } catch (DatabaseOperationException ex) {
 *     // Erreur SQL spécifique
 *     logger.error("Erreur lors de la recherche : " + ex.getOperation());
 * } catch (EntityNotFoundException ex) {
 *     // Entité non trouvée
 *     logger.warn("Restaurant " + ex.getEntityId() + " introuvable");
 * } catch (PersistenceException ex) {
 *     // Autre erreur de persistance
 *     logger.error("Erreur de persistance générale", ex);
 * }
 * </pre>
 *
 * <p><b>Gestion dans les Mappers :</b></p>
 * <pre>
 * public Restaurant findById(int id) {
 *     try {
 *         // Code SQL...
 *     } catch (SQLException ex) {
 *         throw new DatabaseOperationException("FIND by ID", ex);
 *     }
 * }
 * </pre>
 *
 * <p><b>Pattern utilisé :</b> Exception Hierarchy Pattern</p>
 *
 * <p><b>Note importante :</b> Cette classe hérite de RuntimeException (unchecked exception)
 * pour éviter la pollution du code avec des déclarations throws. Les erreurs de persistance
 * sont généralement des erreurs techniques non récupérables au niveau applicatif.</p>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see DatabaseOperationException
 * @see EntityNotFoundException
 * @see RuntimeException
 */
public class PersistenceException extends RuntimeException {

    /**
     * Crée une nouvelle exception de persistance avec un message descriptif.
     *
     * <p>Ce constructeur est utilisé pour les erreurs simples où le message
     * suffit à décrire le problème.</p>
     *
     * <p><b>Exemple :</b></p>
     * <pre>
     * throw new PersistenceException("Impossible de se connecter à la base");
     * </pre>
     *
     * @param message Le message d'erreur décrivant le problème (ne doit pas être null)
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Crée une nouvelle exception de persistance avec un message et une cause.
     *
     * <p>Ce constructeur est utilisé pour encapsuler une exception de plus bas niveau
     * (typiquement une SQLException) tout en ajoutant un contexte métier.</p>
     *
     * <p><b>Avantages :</b></p>
     * <ul>
     *   <li>Préserve la stack trace complète</li>
     *   <li>Permet d'ajouter un contexte métier à une erreur technique</li>
     *   <li>Facilite le debugging (on voit toute la chaîne d'appels)</li>
     * </ul>
     *
     * <p><b>Exemple typique :</b></p>
     * <pre>
     * try {
     *     // Opération SQL
     *     stmt.executeUpdate();
     * } catch (SQLException ex) {
     *     // On encapsule SQLException dans PersistenceException
     *     throw new PersistenceException("Erreur lors de l'insertion", ex);
     * }
     * </pre>
     *
     * <p><b>Stack trace résultante :</b></p>
     * <pre>
     * PersistenceException: Erreur lors de l'insertion
     *     at RestaurantMapper.create(RestaurantMapper.java:45)
     *     ...
     * Caused by: SQLException: ORA-00001: unique constraint violated
     *     at oracle.jdbc.driver.OracleStatement.execute(...)
     *     ...
     * </pre>
     *
     * @param message Le message d'erreur décrivant le problème dans un contexte métier
     * @param cause L'exception d'origine (typiquement SQLException)
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}