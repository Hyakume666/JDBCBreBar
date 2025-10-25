package ch.hearc.ig.guideresto.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Fournit des méthodes utilitaires pour gérer les connexions à la base de données.
 * Idéalement, cette classe devrait également gérer les pools de connexions dans une application plus importante.
 *
 * @author arnaud.geiser
 * @author alain.matile
 */
public class ConnectionUtils {

    private static final Logger logger = LogManager.getLogger();

    private static Connection connection;

    /**
     * Retourne une connexion à la base de données.
     * Les informations de connexion sont chargées depuis le fichier de ressources {@code resources/database.properties}.
     * Si aucune connexion n’existe encore ou si elle a été fermée, une nouvelle est créée.
     *
     * @return un objet {@link Connection} actif vers la base de données.
     */
    public static Connection getConnection() {
        try {
            // Charger les identifiants de la base de données depuis resources/database.properties
            ResourceBundle dbProps = ResourceBundle.getBundle("database");
            String url = dbProps.getString("database.url");
            String username = dbProps.getString("database.username");
            String password = dbProps.getString("database.password");

            logger.info("Tentative de connexion au schéma utilisateur '{}' avec la chaîne JDBC '{}'", username, url);

            // Initialiser une connexion si nécessaire
            if (ConnectionUtils.connection == null || ConnectionUtils.connection.isClosed()) {
                Connection connection = DriverManager.getConnection(url, username, password);
                connection.setAutoCommit(false);
                ConnectionUtils.connection = connection;
            }

        } catch (SQLException | MissingResourceException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return ConnectionUtils.connection;
    }

    /**
     * Ferme proprement la connexion à la base de données si elle est ouverte.
     */
    public static void closeConnection() {
        try {
            if (ConnectionUtils.connection != null && !ConnectionUtils.connection.isClosed()) {
                ConnectionUtils.connection.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
