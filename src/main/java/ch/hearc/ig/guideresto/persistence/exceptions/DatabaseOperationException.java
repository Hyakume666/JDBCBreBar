package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception levée lors d'une erreur SQL (INSERT, UPDATE, DELETE, SELECT).
 * Spécialisation de {@link PersistenceException} pour les erreurs d'opérations SQL.
 *
 * <p>Cette exception encapsule les erreurs SQL en ajoutant le contexte de l'opération
 * qui a échoué. Elle permet de tracer précisément quelle opération CRUD a causé l'erreur.</p>
 *
 * <p><b>Informations capturées :</b></p>
 * <ul>
 *   <li><b>operation :</b> Type d'opération (CREATE, UPDATE, DELETE, FIND, etc.)</li>
 *   <li><b>message :</b> Message d'erreur contextualisé</li>
 *   <li><b>cause :</b> Exception SQL d'origine (SQLException)</li>
 * </ul>
 *
 * <p><b>Types d'opérations couvertes :</b></p>
 * <ul>
 *   <li><b>CREATE :</b> Erreur lors d'un INSERT (violation de contrainte, etc.)</li>
 *   <li><b>UPDATE :</b> Erreur lors d'un UPDATE</li>
 *   <li><b>DELETE :</b> Erreur lors d'un DELETE (contrainte d'intégrité, etc.)</li>
 *   <li><b>FIND :</b> Erreur lors d'un SELECT</li>
 *   <li><b>COUNT :</b> Erreur lors d'un COUNT</li>
 *   <li><b>EXISTS :</b> Erreur lors d'une vérification d'existence</li>
 * </ul>
 *
 * <p><b>Cas d'erreurs SQL typiques :</b></p>
 * <ul>
 *   <li><b>ORA-00001 :</b> Violation de contrainte unique (duplicate key)</li>
 *   <li><b>ORA-02291 :</b> Violation de contrainte d'intégrité (FK inexistante)</li>
 *   <li><b>ORA-02292 :</b> Violation de contrainte d'intégrité (delete avec FK)</li>
 *   <li><b>ORA-01400 :</b> Insertion de NULL dans colonne NOT NULL</li>
 *   <li><b>ORA-01017 :</b> Nom d'utilisateur/mot de passe invalide</li>
 * </ul>
 *
 * <p><b>Exemple d'utilisation dans les Mappers :</b></p>
 * <pre>
 * public Restaurant create(Restaurant restaurant) {
 *     try {
 *         PreparedStatement stmt = conn.prepareStatement(INSERT);
 *         stmt.setString(1, restaurant.getName());
 *         stmt.executeUpdate();
 *         // ...
 *     } catch (SQLException ex) {
 *         // Encapsulation avec contexte
 *         throw new DatabaseOperationException("CREATE Restaurant", ex);
 *     }
 * }
 * </pre>
 *
 * <p><b>Gestion dans les Services :</b></p>
 * <pre>
 * public Restaurant createRestaurant(Restaurant restaurant) {
 *     try {
 *         return restaurantMapper.create(restaurant);
 *     } catch (DatabaseOperationException ex) {
 *         logger.error("Échec de l'opération {} : {}",
 *                      ex.getOperation(), ex.getMessage());
 *         // Rollback
 *         connection.rollback();
 *         return null;
 *     }
 * }
 * </pre>
 *
 * <p><b>Avantages de cette exception :</b></p>
 * <ul>
 *   <li><b>Traçabilité :</b> On sait exactement quelle opération a échoué</li>
 *   <li><b>Logging précis :</b> Messages d'erreur contextualisés</li>
 *   <li><b>Debugging facilité :</b> Stack trace complète + contexte métier</li>
 *   <li><b>Monitoring :</b> Permet de compter les erreurs par type d'opération</li>
 * </ul>
 *
 * <p><b>Format du message généré :</b></p>
 * <pre>
 * "Erreur lors de l'opération [OPERATION]"
 *
 * Exemples :
 * - "Erreur lors de l'opération CREATE Restaurant"
 * - "Erreur lors de l'opération UPDATE City"
 * - "Erreur lors de l'opération DELETE BasicEvaluation"
 * </pre>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see PersistenceException
 * @see EntityNotFoundException
 */
public class DatabaseOperationException extends PersistenceException {

    /**
     * Type d'opération qui a causé l'erreur.
     *
     * <p>Exemples de valeurs :</p>
     * <ul>
     *   <li>"CREATE Restaurant"</li>
     *   <li>"UPDATE City"</li>
     *   <li>"DELETE BasicEvaluation"</li>
     *   <li>"FIND Restaurant by ID"</li>
     *   <li>"FIND ALL Restaurants"</li>
     *   <li>"SEARCH Restaurant by name"</li>
     * </ul>
     */
    private final String operation;

    /**
     * Crée une nouvelle exception d'opération de base de données.
     *
     * <p>Le message d'erreur est automatiquement formaté avec le pattern :</p>
     * <pre>"Erreur lors de l'opération [operation]"</pre>
     *
     * <p><b>Exemple d'utilisation :</b></p>
     * <pre>
     * try {
     *     stmt.executeUpdate();
     * } catch (SQLException ex) {
     *     throw new DatabaseOperationException("CREATE Restaurant", ex);
     * }
     * </pre>
     *
     * <p><b>Message généré :</b></p>
     * <pre>"Erreur lors de l'opération CREATE Restaurant"</pre>
     *
     * <p><b>Stack trace complète :</b></p>
     * <pre>
     * DatabaseOperationException: Erreur lors de l'opération CREATE Restaurant
     *     at RestaurantMapper.create(RestaurantMapper.java:78)
     *     at RestaurantService.createRestaurant(RestaurantService.java:45)
     *     ...
     * Caused by: java.sql.SQLException: ORA-00001: unique constraint violated
     *     at oracle.jdbc.driver.OracleStatement.execute(...)
     *     ...
     * </pre>
     *
     * @param operation Le type d'opération SQL qui a échoué (ex: "CREATE Restaurant")
     * @param cause L'exception SQL d'origine contenant les détails de l'erreur
     */
    public DatabaseOperationException(String operation, Throwable cause) {
        super(String.format("Erreur lors de l'opération %s", operation), cause);
        this.operation = operation;
    }

    /**
     * Retourne le type d'opération qui a causé l'erreur.
     *
     * <p>Cette méthode permet de récupérer le contexte de l'erreur pour
     * le logging, le monitoring ou la gestion d'erreur personnalisée.</p>
     *
     * <p><b>Exemple d'utilisation pour le logging :</b></p>
     * <pre>
     * catch (DatabaseOperationException ex) {
     *     logger.error("Échec de {} : {}",
     *                  ex.getOperation(),      // "CREATE Restaurant"
     *                  ex.getMessage());       // Message complet
     * }
     * </pre>
     *
     * <p><b>Exemple pour des statistiques :</b></p>
     * <pre>
     * catch (DatabaseOperationException ex) {
     *     metrics.incrementErrorCounter(ex.getOperation());
     * }
     * </pre>
     *
     * @return Le type d'opération (ex: "CREATE Restaurant", "UPDATE City")
     */
    public String getOperation() {
        return operation;
    }
}