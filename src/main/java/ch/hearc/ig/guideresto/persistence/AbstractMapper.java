package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Classe abstraite générique pour tous les Mappers.
 * Implémente le pattern Data Mapper avec Identity Map (cache).
 *
 * <p>Cette classe fournit les opérations CRUD de base et gère automatiquement
 * un cache (Identity Map) pour éviter les requêtes redondantes et garantir
 * qu'un même objet en base n'existe qu'une seule fois en mémoire.</p>
 *
 * <p><strong>Fonctionnalités :</strong></p>
 * <ul>
 *   <li><strong>Cache automatique (Identity Map) :</strong> Évite les requêtes
 *       SQL redondantes et assure l'unicité des objets en mémoire</li>
 *   <li><strong>Opérations CRUD génériques :</strong> Create, Read, Update, Delete</li>
 *   <li><strong>Gestion des séquences Oracle :</strong> Récupération automatique
 *       des IDs générés</li>
 *   <li><strong>Logging automatique :</strong> Toutes les opérations sont tracées</li>
 *   <li><strong>Méthodes utilitaires :</strong> exists(), count(), etc.</li>
 * </ul>
 *
 * <p><strong>Pattern Identity Map :</strong><br>
 * Le cache garantit qu'un objet avec un ID donné n'est chargé qu'une seule fois.
 * Les appels suivants à findById() retournent l'instance déjà en cache sans
 * interroger la base de données.</p>
 *
 * <p><strong>Exemple :</strong></p>
 * <pre>
 * // Premier appel : requête SQL + mise en cache
 * Restaurant r1 = RestaurantMapper.getInstance().findById(1);
 *
 * // Second appel : récupération depuis le cache (pas de SQL)
 * Restaurant r2 = RestaurantMapper.getInstance().findById(1);
 *
 * // r1 et r2 pointent vers la même instance
 * assert r1 == r2; // true
 * </pre>
 *
 * <p><strong>Gestion du cache :</strong><br>
 * Le cache est automatiquement maintenu lors des opérations CRUD :
 * <ul>
 *   <li>create() : Ajoute l'objet au cache</li>
 *   <li>update() : Met à jour le cache</li>
 *   <li>delete() : Retire l'objet du cache</li>
 * </ul>
 * </p>
 *
 * <p><strong>Implémentation d'un Mapper concret :</strong></p>
 * <pre>
 * public class RestaurantMapper extends AbstractMapper&lt;Restaurant&gt; {
 *     private static RestaurantMapper instance;
 *
 *     public static RestaurantMapper getInstance() {
 *         if (instance == null) {
 *             instance = new RestaurantMapper();
 *         }
 *         return instance;
 *     }
 *
 *     &#64;Override
 *     protected Restaurant findByIdFromDb(int id) {
 *         // Implémentation de la requête SQL SELECT
 *     }
 *
 *     // Implémentation des autres méthodes abstraites...
 * }
 * </pre>
 *
 * @param <T> Le type d'entité métier (doit implémenter {@link IBusinessObject})
 * @author Votre Nom
 * @version 1.0
 * @since 1.0
 * @see IBusinessObject
 */
public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();

    /**
     * Le cache (Identity Map) générique.
     * Clé : ID de l'entité, Valeur : Instance de l'entité
     */
    private final Map<Integer, T> identityMap = new HashMap<>();

    /**
     * Recherche une entité par son ID.
     * Utilise d'abord le cache (Identity Map), puis interroge la base si nécessaire.
     *
     * <p><strong>Algorithme :</strong></p>
     * <ol>
     *   <li>Vérifier si l'objet est dans le cache</li>
     *   <li>Si oui : retourner l'instance en cache (pas de SQL)</li>
     *   <li>Si non : interroger la base de données via {@link #findByIdFromDb(int)}</li>
     *   <li>Si trouvé en base : ajouter au cache et retourner</li>
     *   <li>Si non trouvé : retourner null</li>
     * </ol>
     *
     * <p><strong>Performance :</strong> Cette méthode évite les requêtes SQL
     * redondantes grâce au cache.</p>
     *
     * @param id L'identifiant unique de l'entité (doit être > 0)
     * @return L'entité trouvée (depuis le cache ou la base), ou null si elle n'existe pas
     * @see #findByIdFromDb(int)
     */
    public T findById(int id) {
        if (identityMap.containsKey(id)) {
            logger.debug("Objet {} trouvé dans le cache de {}", id, this.getClass().getSimpleName());
            return identityMap.get(id);
        }

        T result = findByIdFromDb(id);

        if (result != null) {
            identityMap.put(id, result);
        }
        return result;
    }

    /**
     * Recherche une entité par son ID directement en base de données.
     * Cette méthode doit être implémentée par les classes concrètes.
     *
     * <p><strong>Note :</strong> Cette méthode est appelée par {@link #findById(int)}
     * uniquement si l'objet n'est pas dans le cache.</p>
     *
     * <p><strong>Implémentation typique :</strong></p>
     * <pre>
     * &#64;Override
     * protected Restaurant findByIdFromDb(int id) {
     *     Connection conn = ConnectionUtils.getConnection();
     *     try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
     *         stmt.setInt(1, id);
     *         try (ResultSet rs = stmt.executeQuery()) {
     *             if (rs.next()) {
     *                 return mapResultSetToEntity(rs);
     *             }
     *         }
     *     } catch (SQLException ex) {
     *         logger.error("Erreur SQL", ex);
     *     }
     *     return null;
     * }
     * </pre>
     *
     * @param id L'identifiant unique de l'entité
     * @return L'entité trouvée en base, ou null si elle n'existe pas
     */
    protected abstract T findByIdFromDb(int id);

    /**
     * Récupère toutes les entités de ce type depuis la base de données.
     *
     * <p><strong>Note importante :</strong> Cette méthode charge toutes les entités
     * en mémoire. Sur de grandes tables, cela peut consommer beaucoup de mémoire
     * et de temps. Utiliser avec précaution.</p>
     *
     * <p>Les entités récupérées sont automatiquement ajoutées au cache.</p>
     *
     * @return Un Set contenant toutes les entités. Peut être vide mais jamais null.
     */
    public abstract Set<T> findAll();

    /**
     * Crée une nouvelle entité en base de données.
     * L'ID est généré automatiquement par la séquence Oracle.
     *
     * <p><strong>Comportement attendu :</strong></p>
     * <ol>
     *   <li>Valider que l'objet n'est pas null</li>
     *   <li>Exécuter l'INSERT SQL</li>
     *   <li>Récupérer l'ID généré par la séquence via {@link #getSequenceValue()}</li>
     *   <li>Assigner l'ID à l'objet</li>
     *   <li>Ajouter l'objet au cache via {@link #addToCache(IBusinessObject)}</li>
     *   <li>Retourner l'objet créé</li>
     * </ol>
     *
     * <p><strong>Attention :</strong> Cette méthode ne fait PAS de commit.
     * Le commit doit être géré par la couche Service.</p>
     *
     * @param object L'entité à créer (ne doit pas avoir d'ID)
     * @return L'entité créée avec son ID généré, ou null en cas d'échec
     * @see #getSequenceValue()
     * @see #addToCache(IBusinessObject)
     */
    public abstract T create(T object);

    /**
     * Met à jour une entité existante en base de données.
     *
     * <p><strong>Pré-requis :</strong></p>
     * <ul>
     *   <li>L'objet ne doit pas être null</li>
     *   <li>L'objet doit avoir un ID valide (non null, > 0)</li>
     *   <li>L'entité doit exister en base</li>
     * </ul>
     *
     * <p>Après une mise à jour réussie, l'objet est automatiquement mis à jour
     * dans le cache via {@link #addToCache(IBusinessObject)}.</p>
     *
     * <p><strong>Attention :</strong> Cette méthode ne fait PAS de commit.
     * Le commit doit être géré par la couche Service.</p>
     *
     * @param object L'entité à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi (au moins 1 ligne affectée), false sinon
     * @see #addToCache(IBusinessObject)
     */
    public abstract boolean update(T object);

    /**
     * Supprime une entité de la base de données.
     *
     * <p>Cette méthode délègue à {@link #deleteById(int)} après avoir extrait l'ID.</p>
     *
     * <p><strong>Attention :</strong> Cette méthode ne fait PAS de commit.
     * Le commit doit être géré par la couche Service.</p>
     *
     * @param object L'entité à supprimer (doit avoir un ID valide)
     * @return true si la suppression a réussi, false sinon
     * @see #deleteById(int)
     */
    public abstract boolean delete(T object);

    /**
     * Supprime une entité par son ID.
     *
     * <p><strong>Comportement attendu :</strong></p>
     * <ol>
     *   <li>Exécuter le DELETE SQL</li>
     *   <li>Si succès : retirer l'objet du cache via {@link #removeFromCache(Integer)}</li>
     *   <li>Retourner true si au moins 1 ligne supprimée, false sinon</li>
     * </ol>
     *
     * <p><strong>Attention :</strong> Cette méthode ne fait PAS de commit.
     * Le commit doit être géré par la couche Service.</p>
     *
     * @param id L'identifiant de l'entité à supprimer
     * @return true si la suppression a réussi (au moins 1 ligne affectée), false sinon
     * @see #removeFromCache(Integer)
     */
    public abstract boolean deleteById(int id);

    /**
     * Retourne la requête SQL pour récupérer la valeur actuelle de la séquence Oracle.
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * &#64;Override
     * protected String getSequenceQuery() {
     *     return "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL";
     * }
     * </pre>
     *
     * <p><strong>Note :</strong> Utilise CURRVAL et non NEXTVAL car le trigger
     * a déjà incrémenté la séquence lors de l'INSERT.</p>
     *
     * @return La requête SQL pour récupérer la valeur de la séquence
     * @see #getSequenceValue()
     */
    protected abstract String getSequenceQuery();

    /**
     * Retourne la requête SQL pour vérifier l'existence d'une entité par ID.
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * &#64;Override
     * protected String getExistsQuery() {
     *     return "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
     * }
     * </pre>
     *
     * @return La requête SQL EXISTS
     * @see #exists(int)
     */
    protected abstract String getExistsQuery();

    /**
     * Retourne la requête SQL pour compter le nombre total d'entités.
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * &#64;Override
     * protected String getCountQuery() {
     *     return "SELECT COUNT(*) FROM RESTAURANTS";
     * }
     * </pre>
     *
     * @return La requête SQL COUNT
     * @see #count()
     */
    protected abstract String getCountQuery();

    /**
     * Vérifie si une entité avec l'ID spécifié existe en base de données.
     *
     * <p>Cette méthode interroge directement la base (pas de cache).</p>
     *
     * @param id L'identifiant à vérifier
     * @return true si l'entité existe, false sinon
     * @see #getExistsQuery()
     */
    @SuppressWarnings({"unused","SqlSourceToSinkFlow"})
    public boolean exists(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getExistsQuery())) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la vérification de l'existence pour l'ID {}", id, ex);
        }
        return false;
    }

    /**
     * Compte le nombre total d'entités de ce type en base de données.
     *
     * <p>Cette méthode interroge directement la base (pas de cache).</p>
     *
     * @return Le nombre d'entités, ou 0 en cas d'erreur
     * @see #getCountQuery()
     */
    @SuppressWarnings({"unused","SqlSourceToSinkFlow"})
    public int count() {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getCountQuery());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("Erreur lors du comptage des objets", ex);
            return 0;
        }
    }

    /**
     * Récupère la valeur actuelle de la séquence Oracle (CURRVAL).
     *
     * <p><strong>Utilisation :</strong> Appelée après un INSERT pour récupérer
     * l'ID généré automatiquement par le trigger.</p>
     *
     * <p><strong>Attention :</strong> CURRVAL ne fonctionne que si NEXTVAL a été
     * appelé dans la même session (ce qui est fait par le trigger Oracle).</p>
     *
     * @return La valeur actuelle de la séquence, ou 0 en cas d'erreur
     * @see #getSequenceQuery()
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    protected Integer getSequenceValue() {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getSequenceQuery());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("Erreur lors de la récupération de la valeur de la séquence", ex);
            return 0;
        }
    }

    /**
     * Vérifie si le cache est vide.
     *
     * <p><strong>Utilité :</strong> Pour les tests ou le debugging.</p>
     *
     * @return true si le cache est vide, false sinon
     */
    @SuppressWarnings("unused")
    protected boolean isCacheEmpty() {
        return identityMap.isEmpty();
    }

    /**
     * Vide complètement le cache (Identity Map).
     *
     * <p><strong>Attention :</strong> À utiliser avec précaution.
     * Après un reset, tous les objets seront rechargés depuis la base.</p>
     *
     * <p><strong>Cas d'usage :</strong></p>
     * <ul>
     *   <li>Tests unitaires (réinitialisation entre les tests)</li>
     *   <li>Forcer le rechargement de données modifiées hors application</li>
     * </ul>
     */
    @SuppressWarnings("unused")
    protected void resetCache() {
        identityMap.clear();
        logger.debug("Cache de {} vidé", this.getClass().getSimpleName());
    }

    /**
     * Ajoute ou met à jour un objet dans le cache.
     *
     * <p>Si l'objet a un ID valide (non null), il est ajouté/mis à jour dans le cache.
     * Sinon, l'opération est ignorée.</p>
     *
     * <p><strong>Usage interne :</strong> Appelée automatiquement par les méthodes
     * create(), update() et findById().</p>
     *
     * @param objet L'objet à ajouter au cache (peut être null)
     */
    protected void addToCache(T objet) {
        if (objet != null && objet.getId() != null) {
            identityMap.put(objet.getId(), objet);
        }
    }

    /**
     * Retire un objet du cache par son ID.
     *
     * <p><strong>Usage interne :</strong> Appelée automatiquement par les méthodes
     * delete() et deleteById() après une suppression réussie.</p>
     *
     * @param id L'ID de l'objet à retirer du cache (peut être null)
     */
    protected void removeFromCache(Integer id) {
        if (id != null) {
            identityMap.remove(id);
        }
    }
}