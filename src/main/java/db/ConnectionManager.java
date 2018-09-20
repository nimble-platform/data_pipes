package db;

import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 6/19/18.
 */

public class ConnectionManager {
    private static final Logger logger = Logger.getLogger(ConnectionManager.class);

    private String user;
    private String password;
    private String connectionUrl;

    private Connection connection;

    ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver"); // Check that the driver is ok

            user = System.getenv("DATA_PIPES_POSTGRES_USERNAME");
            password = System.getenv("DATA_PIPES_POSTGRES_PASSWORD");
            String url = System.getenv("DATA_PIPES_POSTGRES_URL");
            if (isNullOrEmpty(user) || isNullOrEmpty(password) || isNullOrEmpty(url)) {
                throw new IllegalArgumentException("Credential values can't be null or empty");
            }

            connectionUrl = "jdbc:postgresql://" + url;
            connection = DriverManager.getConnection(connectionUrl, user, password);
        } catch (Exception e) {
            logger.error("Error during the initialization of the DB Connection class", e);
            System.exit(1);
        }
    }


    public boolean isConnected() {
        try {
            return connection.isValid(1000);
        } catch (SQLException e) {
            logger.error("Error during check that the connection is alive", e);
            return false;
        }
    }

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        try {
            return connection.prepareStatement(sqlQuery);
        } catch (PSQLException e) {
            logger.error("Error during creation of prepared statement - trying to recreate connection again");
            recreateConnection();

            return connection.prepareStatement(sqlQuery);
        }
    }

    private void recreateConnection() throws SQLException {
        try {
            logger.info("Recreating the connection");
            connection = DriverManager.getConnection(connectionUrl, user, password);

            logger.info("Verifying the connection is valid");
            if (!connection.isValid(1000)) {
                logger.error("Created a new connection but the it's not valid");
            }
        } catch (SQLException e) {
            logger.info("Error during creation of a new connection", e);
            throw e;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        try {
            return connection.getMetaData();
        } catch (SQLException e) {
            logger.error("Error during retrieval of the metadata, trying to recreate connection again");
            recreateConnection();

            return connection.getMetaData();
        }
    }

    public boolean reconnect() {
        try {
            connection = DriverManager.getConnection(connectionUrl, user, password);
            return connection.isValid(1000);
        } catch (SQLException e) {
            logger.error(e);
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            logger.info("Closing db connection");
            connection.close();
        } catch (SQLException e) {
            logger.error("Error during closing the db connection");
            e.printStackTrace();
        }
    }
}
