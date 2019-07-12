package eu.nimble.service.datapipes.rest.check;

import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.db.ConnectionManager;
import eu.nimble.service.datapipes.db.DBManager;
import eu.nimble.service.datapipes.kafka.KafkaHelper;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Created by evgeniyh on 5/8/18.
 * modified by Andrea Musumeci
 */


@Controller
@RequestMapping(path = "/check")
@Api("Data Pipes service API")
public class DataPipeCheckService implements DataPipesCheckApi {
    private static final Logger logger = Logger.getLogger(DataPipeCheckService.class);

    public static DBManager dbManager = createDBManager();
    private static ConnectionManager connection = dbManager.getConnectionManager();

    public DataPipeCheckService() {
        logger.info("starting Main...");

        if (Configurations.enableStream) {
            KafkaHelper.startKafkaStreams();
            KafkaHelper.startDbLoggerKafkaConsumer();
            KafkaHelper.createNewTopic(Configurations.STREAMS_OUTPUT_TOPIC);
            KafkaHelper.createNewTopic(Configurations.STREAMS_INPUT_TOPIC);
        }
    }

    public static void main(String[] args) {
        new DataPipeCheckService();
    }

    public ResponseEntity<?> getHello() {
        return new ResponseEntity<>( new ResponseCheck("Hello from Internal DataChannel Service")  , HttpStatus.OK);
    }

    public ResponseEntity<?> runHealthCheck() {
        logger.info("Verifying the DB connection is connected");
        if (connection.isConnected()) {
            return new ResponseEntity<>( new ResponseCheck("Running")  , HttpStatus.OK);
        }
        logger.error("The connection wasn't alive - trying to reconnect");

        return (connection.reconnect()) ? new ResponseEntity<>( new ResponseCheck("Running") , HttpStatus.OK) :
        new ResponseEntity<>( new ResponseCheck("Failed")  , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<?> runDBReconnect() {
        logger.info("Running a reconnection to the DB");
        return connection.reconnect() ? new ResponseEntity<>( new ResponseCheck("Reconnected") , HttpStatus.OK) :
        new ResponseEntity<>( new ResponseCheck("Failed")  , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static DBManager createDBManager() {
        try {
            return new DBManager(Configurations.CHANNELS_TABLE, Configurations.DATA_TABLE);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("Error during the creation of the db manager");
        }
    }
    
}
