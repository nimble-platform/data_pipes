package rest;

import common.Configurations;
import common.Helper;
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

        String topicName = Configurations.OUTPUT_TOPIC_PREFIX + channelId;
        String postUrl = String.format("%s%s", Configurations.CSB_CREATE_TOPIC_URL, topicName);

        try {
            logger.info("Sending post command to CSB to create new topic on - " + postUrl);
            Helper.executeHttpPost(postUrl, true, true);
            logger.info("Post command successful - inserting new DB record"); // TODO: handle delete topic if command fails

            dbManager.addNewChannel(channelId, source, target, jsonFilter);
            logger.info("Successfully inserted new filter into the DB");
        } catch (Exception e) {
            logger.error("Error during start of a new channel", e);
            return createResponse(Status.INTERNAL_SERVER_ERROR, "ERROR !!! " + e.toString());
        }

        return createResponse(Status.OK, channelId.toString());
    }
}
