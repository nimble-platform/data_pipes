package db;

import common.Configurations;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.security.InvalidParameterException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class DBManager implements Closeable {
    private static final Logger logger = Logger.getLogger(DBManager.class);

    private final ConnectionManager connection = new ConnectionManager();
    private final QueriesManager qm;

    private HashSet<String> supportedTables = new HashSet<>();

    public DBManager(String channelsTableName, String dataTableName) throws Exception {
        this.qm = new QueriesManager(channelsTableName, dataTableName); // Will check the arguments

        DatabaseMetaData dbm = connection.getMetaData();

        createTableIfMissing(dbm, channelsTableName, qm.CREATE_CHANNELS_TABLE);
        createTableIfMissing(dbm, dataTableName, qm.CREATE_DATA_TABLE);

        supportedTables.add(channelsTableName);
        supportedTables.add(dataTableName);
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

    public ResultSet readAllTable(String tableName) throws SQLException {
        if (!supportedTables.contains(tableName)) {
            logger.error("Not supported table name - " + tableName);
            throw new InvalidParameterException("Table isn't supported - " + tableName);
        }

        String selectAll = qm.getReadAllTableQuery(tableName);
        PreparedStatement ps = connection.prepareStatement(selectAll);

        logger.info("Executing query - " + ps);
        return ps.executeQuery();
    }

    @Override
    public void close() {
        connection.close();
    }

    public ConnectionManager getConnectionManager() {
        return connection;
    }
}
