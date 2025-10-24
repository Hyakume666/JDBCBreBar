package ch.hearc.ig.guideresto.persistence.exceptions;

/**
 * Exception levée lorsqu'une entité demandée n'existe pas en base de données.
 * Spécialisation de {@link PersistenceException} pour les cas de recherche infructueuse.
 *
 * <p>Cette exception est levée quand une opération tente d'accéder à une entité
 * par son ID mais que celle-ci n'existe pas (ou plus) en base de données.</p>
 *
 * <p><b>Informations capturées :</b></p>
 * <ul>
 *   <li><b>entityType :</b> Type de l'entité recherchée (ex: "Restaurant", "City")</li>
 *   <li><b>entityId :</b> ID de l'entité qui n'a pas été trouvée</li>
 *   <li><b>message :</b> Message formaté automatiquement</li>
 * </ul>
 *
 * <p><b>Cas d'utilisation typiques :</b></p>
 * <ul>
 *   <li>findById() retourne null pour un ID inexistant</li>
 *   <li>Tentative d'update sur une entité supprimée</li>
 *   <li>Chargement d'une relation (FK) pointant vers une entité supprimée</li>
 *   <li>Accès à une ressource via une URL avec un ID invalide</li>
 * </ul>
 *
 * <p><b>Différence avec null :</b></p>
 * <ul>
 *   <li><b>Retourner null :</b> Approche silencieuse, risque de NullPointerException</li>
 *   <li><b>Lever EntityNotFoundException :</b> Erreur explicite, gestion centralisée</li>
 * </ul>
 *
 * <p><b>Exemple d'utilisation dans un Mapper :</b></p>
 * <pre>
 * public Restaurant findById(int id) {
 *     Restaurant restaurant = findByIdFromDb(id);
 *     if (restaurant == null) {
 *         throw new EntityNotFoundException("Restaurant", id);
 *     }
 *     return restaurant;
 * }
 * </pre>
 *
 * <p><b>Exemple dans un Service :</b></p>
 * <pre>
 * public boolean updateRestaurant(Restaurant restaurant) {
 *     try {
 *         // Vérifier que le restaurant existe
 *         if (!restaurantMapper.exists(restaurant.getId())) {
 *             throw new EntityNotFoundException("Restaurant", restaurant.getId());
 *         }
 *         return restaurantMapper.update(restaurant);
 *     } catch (EntityNotFoundException ex) {
 *         logger.warn("Tentative de mise à jour d'un restaurant inexistant : {}",
 *                     ex.getEntityId());
 *         return false;
 *     }
 * }
 * </pre>
 *
 * <p><b>Gestion dans la couche Présentation :</b></p>
 * <pre>
 * try {
 *     Restaurant restaurant = restaurantService.getRestaurantById(id);
 *     showRestaurant(restaurant);
 * } catch (EntityNotFoundException ex) {
 *     System.out.println("❌ " + ex.getMessage());
 *     // "Restaurant avec l'ID 999 introuvable"
 * }
 * </pre>
 *
 * <p><b>Exemple avec API REST (bonus) :</b></p>
 * <pre>
 * &#64;GetMapping("/restaurants/{id}")
 * public ResponseEntity&lt;Restaurant&gt; getRestaurant(@PathVariable int id) {
 *     try {
 *         Restaurant restaurant = restaurantService.findById(id);
 *         return ResponseEntity.ok(restaurant);
 *     } catch (EntityNotFoundException ex) {
 *         return ResponseEntity.notFound().build();  // HTTP 404
 *     }
 * }
 * </pre>
 *
 * <p><b>Avantages de cette exception :</b></p>
 * <ul>
 *   <li><b>Clarté :</b> Erreur explicite vs null ambiguë</li>
 *   <li><b>Traçabilité :</b> On sait exactement quelle entité et quel ID</li>
 *   <li><b>Gestion centralisée :</b> Try-catch au bon niveau</li>
 *   <li><b>Messages utilisateur :</b> Formatage automatique des messages</li>
 * </ul>
 *
 * <p><b>Format du message généré :</b></p>
 * <pre>
 * "[EntityType] avec l'ID [entityId] introuvable"
 *
 * Exemples :
 * - "Restaurant avec l'ID 999 introuvable"
 * - "City avec l'ID 42 introuvable"
 * - "EvaluationCriteria avec l'ID 5 introuvable"
 * </pre>
 *
 * <p><b>Pattern utilisé :</b> Exception spécialisée pour cas métier</p>
 *
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see PersistenceException
 * @see DatabaseOperationException
 */
public class EntityNotFoundException extends PersistenceException {

    /**
     * ID de l'entité qui n'a pas été trouvée.
     * Permet d'identifier précisément quelle entité était recherchée.
     */
    private final Integer entityId;

    /**
     * Type de l'entité recherchée (nom de la classe).
     *
     * <p>Exemples de valeurs :</p>
     * <ul>
     *   <li>"Restaurant"</li>
     *   <li>"City"</li>
     *   <li>"RestaurantType"</li>
     *   <li>"CompleteEvaluation"</li>
     *   <li>"EvaluationCriteria"</li>
     * </ul>
     */
    private final String entityType;

    /**
     * Crée une nouvelle exception pour une entité non trouvée.
     *
     * <p>Le message d'erreur est automatiquement formaté avec le pattern :</p>
     * <pre>"[entityType] avec l'ID [entityId] introuvable"</pre>
     *
     * <p><b>Exemple d'utilisation :</b></p>
     * <pre>
     * Restaurant restaurant = restaurantMapper.findById(999);
     * if (restaurant == null) {
     *     throw new EntityNotFoundException("Restaurant", 999);
     * }
     * </pre>
     *
     * <p><b>Message généré :</b></p>
     * <pre>"Restaurant avec l'ID 999 introuvable"</pre>
     *
     * <p><b>Stack trace :</b></p>
     * <pre>
     * EntityNotFoundException: Restaurant avec l'ID 999 introuvable
     *     at RestaurantMapper.findById(RestaurantMapper.java:125)
     *     at RestaurantService.getRestaurantById(RestaurantService.java:78)
     *     at Application.showRestaurant(Application.java:234)
     *     ...
     * </pre>
     *
     * <p><b>Convention de nommage :</b></p>
     * <ul>
     *   <li>Utiliser le nom de la classe (ex: "Restaurant", pas "restaurant")</li>
     *   <li>Utiliser le nom simple, pas le nom qualifié (ex: "Restaurant", pas "ch.hearc...Restaurant")</li>
     * </ul>
     *
     * @param entityType Le type de l'entité recherchée (ex: "Restaurant")
     * @param entityId L'ID de l'entité qui n'a pas été trouvée
     */
    public EntityNotFoundException(String entityType, Integer entityId) {
        super(String.format("%s avec l'ID %d introuvable", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Retourne l'ID de l'entité qui n'a pas été trouvée.
     *
     * <p>Cette méthode permet de récupérer l'ID pour le logging,
     * l'affichage à l'utilisateur ou des traitements particuliers.</p>
     *
     * <p><b>Exemple pour le logging :</b></p>
     * <pre>
     * catch (EntityNotFoundException ex) {
     *     logger.warn("Entité {} avec ID {} introuvable",
     *                 ex.getEntityType(),
     *                 ex.getEntityId());
     * }
     * </pre>
     *
     * <p><b>Exemple pour un message utilisateur :</b></p>
     * <pre>
     * catch (EntityNotFoundException ex) {
     *     System.out.printf("❌ Le %s #%d n'existe pas ou a été supprimé.%n",
     *                       ex.getEntityType(),
     *                       ex.getEntityId());
     * }
     * </pre>
     *
     * @return L'ID de l'entité recherchée
     */
    public Integer getEntityId() {
        return entityId;
    }

    /**
     * Retourne le type de l'entité qui n'a pas été trouvée.
     *
     * <p>Cette méthode permet de savoir quel type d'entité était recherchée,
     * utile pour la gestion d'erreur personnalisée ou le logging.</p>
     *
     * <p><b>Exemple pour un switch :</b></p>
     * <pre>
     * catch (EntityNotFoundException ex) {
     *     switch (ex.getEntityType()) {
     *         case "Restaurant" -&gt; showRestaurantNotFoundMessage();
     *         case "City" -&gt; showCityNotFoundMessage();
     *         default -&gt; showGenericNotFoundMessage();
     *     }
     * }
     * </pre>
     *
     * <p><b>Exemple pour des statistiques :</b></p>
     * <pre>
     * catch (EntityNotFoundException ex) {
     *     metrics.incrementNotFoundCounter(ex.getEntityType());
     * }
     * </pre>
     *
     * @return Le type de l'entité (ex: "Restaurant", "City")
     */
    public String getEntityType() {
        return entityType;
    }
}