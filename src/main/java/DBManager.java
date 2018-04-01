import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class DBManager {
    private static final Logger logger = Logger.getLogger(DBManager.class);

    private final String urlTemplate = "jdbc:postgresql://";
    private final String pipesTableName;

    private final String CREATE_PIPES_TABLE_TEMPLATE =
            "CREATE TABLE %s (" +
                    "uid  UUID  NOT NULL ," +
                    "json TEXT  NOT NULL ," +
                    "PRIMARY KEY(uid) );";

    private final String INSERT_INTO_TABLE_TEMPLATE = "INSERT INTO %s VALUES (?,?);";
    private final String GET_DATA_FROM_TABLE_TEMPLATE = "SELECT json FROM %s WHERE uid=?;";

    private final Connection connection;

    public DBManager(String pipesTableName) throws Exception {
        if (pipesTableName == null || pipesTableName.isEmpty()) {
            throw new NullPointerException("Pipes table name can't be null or empty");
        }
        this.pipesTableName = pipesTableName;

        Class.forName("org.postgresql.Driver"); // Check that the driver is ok

        String user = System.getenv("POSTGRES_USERNAME");
        String password = System.getenv("POSTGRES_PASSWORD");
        String url = System.getenv("POSTGRES_URL");

        if (user == null || password == null || url == null || user.isEmpty() || password.isEmpty() || url.isEmpty()) {
            throw new Exception("Credential values can't be null or empty");
        }

        connection = DriverManager.getConnection(urlTemplate + url, user, password);

        DatabaseMetaData dbm = connection.getMetaData();
        createTableIfMissing(dbm, pipesTableName, String.format(CREATE_PIPES_TABLE_TEMPLATE, pipesTableName));
    }

    public String getDataPipeJson(UUID uuid) throws SQLException {
        String statement = String.format(GET_DATA_FROM_TABLE_TEMPLATE, pipesTableName);
        PreparedStatement ps = connection.prepareStatement(statement);

        ps.setObject(1, uuid);
        logger.info("Executing query - " + ps);

        ResultSet rs = ps.executeQuery();

        if (!rs.isBeforeFirst()) {
            logger.error("Query completed successfully - result was empty !!!");
            throw new IllegalArgumentException("UUID = " + uuid.toString() + " Has no data in the table");

        }
        String data = null;
        while (rs.next()) {
            if (data != null) {
                logger.error("Received two rows for uuid = " + uuid);
                throw new IllegalStateException("Received two rows for uuid = " + uuid);
            }
            data = rs.getString(1);
        }

        return data;
    }

    public UUID addNewDataPipeFilter(String dataJson) throws SQLException {
        UUID uuid = UUID.randomUUID();
        String statement = String.format(INSERT_INTO_TABLE_TEMPLATE, pipesTableName);
        PreparedStatement ps = connection.prepareStatement(statement);

        ps.setObject(1, uuid);
        ps.setString(2, dataJson);

        executeUpdateStatement(ps, true);

        return uuid;
    }

    private void createTableIfMissing(DatabaseMetaData dbm, String tableName, String createTableQuery) throws SQLException {
        logger.info("Verifying table with name - " + tableName + " exists");
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
            logger.info(String.format("The update statement completed successfully and affected %d lines", affectedRows));
        }
    }
}
