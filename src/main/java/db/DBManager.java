package db;

import common.Configurations;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class DBManager implements Closeable{
    private static final Logger logger = Logger.getLogger(DBManager.class);

    private Connection connection;
    private final QueriesManager qm;

    private final String connectionUrl;
    private final String password;
    private final String user;

    public DBManager(String channelsTableName, String dataTableName) throws Exception {
        this.qm = new QueriesManager(channelsTableName, dataTableName); // Will check the arguments

        Class.forName("org.postgresql.Driver"); // Check that the driver is ok

        user = System.getenv("POSTGRES_USERNAME");
        password = System.getenv("POSTGRES_PASSWORD");
        String url = System.getenv("POSTGRES_URL");
        if (isNullOrEmpty(user) || isNullOrEmpty(password) || isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Credential values can't be null or empty");
        }

        connectionUrl = "jdbc:postgresql://" + url;
        connection = DriverManager.getConnection(connectionUrl, user, password);

        DatabaseMetaData dbm = connection.getMetaData();

        createTableIfMissing(dbm, channelsTableName, qm.CREATE_CHANNELS_TABLE);
        createTableIfMissing(dbm, dataTableName, qm.CREATE_DATA_TABLE);
    }

    public String getFilterJson(UUID channelId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.GET_FILTER);

        ps.setObject(1, channelId);
        logger.info("Executing query - " + ps);

        ResultSet rs = ps.executeQuery();

        if (!rs.isBeforeFirst()) {
            logger.error("Query completed successfully - result was empty !!!");
            throw new IllegalArgumentException("UUID = " + channelId.toString() + " Has no data in the table");
        }
        String data = null;
        while (rs.next()) {
            if (data != null) {
                logger.error("Received two rows for channelId = " + channelId);
                throw new IllegalStateException("Received two rows for channelId = " + channelId);
            }
            data = rs.getString(1);
        }

        logger.info("The received data was - " + data);
        return data;
    }

    public void addNewChannel(UUID channelId, String source, String target, String jsonFilter) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.INSERT_INTO_CHANNELS);

        ps.setObject(1, channelId);
        ps.setString(2, source);
        ps.setString(3, target);
        ps.setString(4, jsonFilter);

        executeUpdateStatement(ps, true);
    }

    private void createTableIfMissing(DatabaseMetaData dbm, String tableName, String createTableQuery) throws SQLException {
//        logger.info("Verifying table with name - " + tableName + " exists");
        ResultSet tables = dbm.getTables(null, null, tableName, null);

        if (!tables.next()) {
            logger.error("ERROR !!! The table - " + tableName + " doesn't exists - creating it now");
            PreparedStatement ps = connection.prepareStatement(createTableQuery);
            executeUpdateStatement(ps, false);
        }

//        logger.info("SUCCESS !!! The table - " + tableName + " exists");
    }

    private void executeUpdateStatement(PreparedStatement ps, boolean silent) throws SQLException {
        if (!silent) {
            logger.info("Executing update statement - " + ps);
        }
        if (ps == null) {
            throw new NullPointerException("Failed to create statement");
        }
        int affectedRows = ps.executeUpdate();

        if (!silent) {
            logger.info(String.format("The update statement completed successfully and affected %d rows", affectedRows));
        }
        ps.close();
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

    public void addNewData(UUID channelId, String data) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.INSERT_INTO_DATA);

        ps.setObject(1, channelId);
        ps.setLong(2, System.currentTimeMillis());
        ps.setString(3, data);

        executeUpdateStatement(ps, false);
    }

    public void deleteTables() throws SQLException {
        logger.info("Deleting both of the tables");
        if (!Configurations.ENVIRONMENT.equals("dev")) {
            logger.error("Deleting tables is supported only in dev environment");
            return;
        }

        PreparedStatement ps = connection.prepareStatement(qm.DELETE_DATA_TABLE);
        executeUpdateStatement(ps, false);

        ps = connection.prepareStatement(qm.DELETE_CHANNELS_TABLE);
        executeUpdateStatement(ps, false);
    }

    public ResultSet getChannelsForTarget(String target) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.GET_CHANNELS);

        ps.setObject(1, target);
        logger.info("Executing query - " + ps);

        return ps.executeQuery();
    }

    public ResultSet getDataForChannelId(UUID channelId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.GET_MESSAGES);

        ps.setObject(1, channelId);
        logger.info("Executing query - " + ps);

        return ps.executeQuery();
    }

    @Override
    public void close()  {
        try {
            logger.info("Closing db connection");
            connection.close();
        } catch (SQLException e) {
            logger.error("Error during closing the db connection");
            e.printStackTrace();
        }
    }

    public void deleteMessages(UUID channelUuid) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.DELETE_CHANNEL);
        ps.setObject(1, channelUuid);

        executeUpdateStatement(ps, false);
    }

    public void deleteChannel(UUID channelUuid) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(qm.DELETE_MESSAGES);
        ps.setObject(1, channelUuid);

        executeUpdateStatement(ps, false);
    }
}
