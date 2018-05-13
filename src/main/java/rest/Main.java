package rest;

import common.Configurations;
import common.KafkaHandler;
import db.DBManager;
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
        KafkaHandler.startKafkaStreams();
        KafkaHandler.startDbLoggerKafkaConsumer();
    }

    @GET
    public Response getHello() {
        return createResponse(Status.OK, "Hello from Data-Channels Service");
    }

    @GET
    @Path("/health-check")
    public Response runHealthCheck() {
        logger.info("Run test against the DB and message hub");
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
    }

    private static DBManager createDBManager() {
        try {
            return new DBManager(Configurations.CHANNELS_TABLE, Configurations.DATA_TABLE);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException("Error during creationg of the db manager");
        }
    }
}
