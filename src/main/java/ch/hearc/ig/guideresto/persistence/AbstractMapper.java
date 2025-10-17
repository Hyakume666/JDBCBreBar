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

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();

    /**
     * Le cache (Identity Map) générique
     */
    private final Map<Integer, T> identityMap = new HashMap<>();

    /**
     * @param id L'ID de l'objet à rechercher.
     * @return L'objet trouvé, soit depuis le cache, soit depuis la base de données.
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
     * @param id L'ID de l'objet à rechercher en base de données.
     * @return L'objet trouvé en base de données, ou null.
     */
    protected abstract T findByIdFromDb(int id);

    public abstract Set<T> findAll();
    public abstract T create(T object);
    public abstract boolean update(T object);
    public abstract boolean delete(T object);
    public abstract boolean deleteById(int id);

    protected abstract String getSequenceQuery();
    protected abstract String getExistsQuery();
    protected abstract String getCountQuery();

    public boolean exists(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getExistsQuery())) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    public int count() {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getCountQuery());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    protected Integer getSequenceValue() {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(getSequenceQuery());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    protected boolean isCacheEmpty() {
        return identityMap.isEmpty();
    }

    protected void resetCache() {
        identityMap.clear();
        logger.debug("Cache de {} vidé", this.getClass().getSimpleName());
    }

    protected void addToCache(T objet) {
        if (objet != null && objet.getId() != null) {
            identityMap.put(objet.getId(), objet);
        }
    }

    protected void removeFromCache(Integer id) {
        if (id != null) {
            identityMap.remove(id);
        }
    }
}