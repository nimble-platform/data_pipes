package db;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 5/8/18.
 */

public class QueriesManager {
    private static final Logger logger = Logger.getLogger(QueriesManager.class);

    final String GET_CHANNELS;
    final String GET_MESSAGES;

    final String CREATE_CHANNELS_TABLE;
    final String CREATE_DATA_TABLE;

    final String INSERT_INTO_CHANNELS;
    final String INSERT_INTO_DATA;

    final String GET_CHANNEL;

    final String DELETE_DATA_TABLE;
    final String DELETE_CHANNELS_TABLE;

    final String DELETE_CHANNEL;
    final String DELETE_MESSAGES;

    private Map<String, String> nameToQueryAll = new HashMap<>();

    public QueriesManager(String channelsTableName, String dataTableName) {
        if (isNullOrEmpty(channelsTableName) || isNullOrEmpty(dataTableName)) {
            throw new NullPointerException("Table names can't be null or empty");
        }

        nameToQueryAll.put(channelsTableName, String.format("SELECT * FROM %s;", channelsTableName));
        nameToQueryAll.put(dataTableName, String.format("SELECT * FROM %s;", dataTableName));

        logger.info(String.format("Channels table = '%s' , DataRequests table = '%s'", channelsTableName, dataTableName));

        CREATE_CHANNELS_TABLE = String.format(
                "CREATE TABLE %s " +
                        "( c_id   UUID  NOT NULL ," +
                        "  source TEXT  NOT NULL ," +
                        "  target TEXT  NOT NULL ," +
                        "  filter TEXT  NOT NULL ," +
                        "  PRIMARY KEY(c_id) );", channelsTableName);

        CREATE_DATA_TABLE = String.format(
                "CREATE TABLE %s " +
                        "( c_id UUID   NOT NULL ," +
                        "  time BIGINT NOT NULL ," +
                        "  data TEXT   NOT NULL ," +
                        "  PRIMARY KEY(time, c_id) );", dataTableName);

        INSERT_INTO_CHANNELS = String.format("INSERT INTO %s VALUES (?,?,?,?);", channelsTableName);
        INSERT_INTO_DATA = String.format("INSERT INTO %s VALUES (?,?,?);", dataTableName);

        GET_CHANNEL = String.format("SELECT * FROM %s WHERE c_id=?;", channelsTableName);

        DELETE_DATA_TABLE = String.format("DROP TABLE %s ;", dataTableName);
        DELETE_CHANNELS_TABLE = String.format("DROP TABLE %s ;", channelsTableName);

        GET_CHANNELS = String.format("SELECT c_id FROM %s WHERE target=? ;", channelsTableName);
        GET_MESSAGES = String.format("SELECT data FROM %s WHERE c_id=? ;", dataTableName);

        DELETE_CHANNEL = String.format("DELETE FROM %s WHERE c_id=? ;", channelsTableName);
        DELETE_MESSAGES = String.format("DELETE FROM %s WHERE c_id=? ;", dataTableName);

        logger.info("The delete channel query is - " + DELETE_CHANNEL);
        logger.info("The delete messages query is - " + DELETE_MESSAGES);

        logger.info("The get all channel ids for target query is - " + GET_CHANNELS);
        logger.info("The get all message for channel id query is - " + GET_MESSAGES);

        logger.info("The delete channels table query is - " + DELETE_CHANNELS_TABLE);
        logger.info("The delete data table query is - " + DELETE_DATA_TABLE);

        logger.info("The create channels table query is - " + CREATE_CHANNELS_TABLE);
        logger.info("The create data table query is - " + CREATE_DATA_TABLE);

        logger.info("The insert into channels table query is - " + INSERT_INTO_CHANNELS);
        logger.info("The insert into data table query is - " + INSERT_INTO_DATA);

        logger.info("The get channel for channel id query is - " + GET_CHANNEL);
    }

    public String getReadAllTableQuery(String tableName) {
        return nameToQueryAll.get(tableName);
    }
}
