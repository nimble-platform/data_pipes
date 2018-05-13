package db;

import org.apache.log4j.Logger;

import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 5/8/18.
 */

public class QueriesManager {
    private static final Logger logger = Logger.getLogger(QueriesManager.class);

    final String CREATE_CHANNELS_TABLE;
    final String CREATE_DATA_TABLE;

    final String INSERT_INTO_CHANNELS;
    final String INSERT_INTO_DATA;

    final String GET_FILTER;
    final String GET_DATA;

    final String DELETE_DATA_TABLE;
    final String DELETE_CHANNELS_TABLE;

    public QueriesManager(String channelsTableName, String dataTableName) {
        if (isNullOrEmpty(channelsTableName) || isNullOrEmpty(dataTableName)) {
            throw new NullPointerException("Table names can't be null or empty");
        }
        logger.info(String.format("Channels table = '%s' , Data table = '%s'", channelsTableName, dataTableName));

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

        GET_FILTER = String.format("SELECT filter FROM %s WHERE c_id=?;", channelsTableName);
        GET_DATA = String.format("SELECT data FROM %s WHERE c_id=?;", dataTableName);

        DELETE_DATA_TABLE = String.format("DROP TABLE %s ;", dataTableName);
        DELETE_CHANNELS_TABLE = String.format("DROP TABLE %s ;", channelsTableName);

        logger.info("The delete channels table query is - " + DELETE_CHANNELS_TABLE);
        logger.info("The delete data table query is - " + DELETE_DATA_TABLE);

        logger.info("The create channels table query is - " + CREATE_CHANNELS_TABLE);
        logger.info("The create data table query is - " + CREATE_DATA_TABLE);

        logger.info("The insert into channels table query is - " + INSERT_INTO_CHANNELS);
        logger.info("The insert into data table query is - " + INSERT_INTO_DATA);

        logger.info("The get filter query is - " + GET_FILTER);
        logger.info("The get data query is - " + GET_DATA);
    }
}
