package rest;

import common.Configurations;
import db.DBManager;
import kafka.KafkaHelper;
import org.apache.log4j.Logger;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static common.Helper.createResponse;

/**
 * Created by evgeniyh on 5/8/18.
 */
@ApplicationPath("/")
@Path("/")
@WebListener
@Singleton
public class Main extends Application implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static DBManager dbManager = createDBManager();

    public Main() {
        KafkaHelper.startKafkaStreams();
        KafkaHelper.startDbLoggerKafkaConsumer();
        KafkaHelper.createNewTopic(Configurations.STREAMS_OUTPUT_TOPIC);
        KafkaHelper.createNewTopic(Configurations.STREAMS_INPUT_TOPIC);
    }

    @GET
    public Response getHello() {
        return createResponse(Status.OK, "Hello from Data-Channels Service");
    }

    @GET
    @Path("/health-check")
    public Response runHealthCheck() {
        logger.info("TODO !!! Run test against the DB and message hub");
//        Enumeration loggers = LogManager.getCurrentLoggers();
//
//        while(loggers.hasMoreElements()) {
//            Category c = (Category) loggers.nextElement();
//            System.out.println(c.getName());
//            Enumeration appenders = c.getAllAppenders();
//            while(appenders.hasMoreElements()) {
//                Appender a = (Appender) appenders.nextElement();
//                System.out.println(a.getName());
//            }
//        }
//        System.exit(1);
        return createResponse(Status.OK, "OK");
    }

    @POST
    @Path("/reconnect-db")
    public Response runDBReconnect() {
        logger.info("Running a DB manager reconnect");
        return dbManager.reconnect() ?
                createResponse(Status.OK, "Reconnected") :
                createResponse(Status.INTERNAL_SERVER_ERROR, "Failed to reconnect");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroyed");
        dbManager.close();
    }

    private static DBManager createDBManager() {
        try {
            return new DBManager(Configurations.CHANNELS_TABLE, Configurations.DATA_TABLE);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException("Error during the creation of the db manager");
        }
    }
}
