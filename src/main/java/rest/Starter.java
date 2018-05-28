package rest;

import common.Helper;
import kafka.KafkaHelper;
import org.apache.log4j.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static common.Helper.createResponse;
import static common.Helper.isNullOrEmpty;
import static rest.Main.dbManager;

/**
 * Created by evgeniyh on 5/8/18.
 */

// TODO: add body as json configurations
// TODO: handle delete topic if command fails
@Path("/start-new")
public class Starter {
    private static final Logger logger = Logger.getLogger(Starter.class);

    @POST
    public Response startNewChannel(@QueryParam("source") String source,
                                    @QueryParam("target") String target,
                                    @QueryParam("filter") String jsonFilter) {

        if (isNullOrEmpty(source) || isNullOrEmpty(target) || isNullOrEmpty(jsonFilter)) {
            return createResponse(Status.BAD_REQUEST, "Must provide source target and filter parameters");
        }

        logger.info(String.format("Received POST command on start-new with params source=%s, target=%s, filter=%s", source, target, jsonFilter));

        UUID channelId = UUID.randomUUID();
        logger.info("Starting new channel with id - " + channelId);

        try {
            String topicName = Helper.generateOutputTopicName(channelId);

            KafkaHelper.createNewTopic(topicName);
            logger.info("Output topic for the channel was created successfully - " + topicName);

            dbManager.addNewChannel(channelId, source, target, jsonFilter);
            logger.info("Successfully inserted new filter into the DB");
        } catch (Exception e) {
            logger.error("Error during start of a new channel", e);
            return createResponse(Status.INTERNAL_SERVER_ERROR, "ERROR !!! " + e.toString());
        }

        return createResponse(Status.OK, channelId.toString());
    }
}
