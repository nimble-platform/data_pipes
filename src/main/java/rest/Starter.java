package rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.Helper;
import kafka.KafkaHelper;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static common.Helper.createResponse;
import static common.Helper.isNullOrEmpty;
import static rest.Main.dbManager;

/**
 * Created by evgeniyh on 5/8/18.
 */

// TODO: handle delete topic if command fails
@Path("/start-new")
public class Starter {
    private static final Logger logger = Logger.getLogger(Starter.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startNewChannel(String configs,
                                    @QueryParam("source") String source,
                                    @QueryParam("target") String target,
                                    @QueryParam("filter") String jsonFilter) {
        try {
            if (!isNullOrEmpty(configs)) {
                StartChannelConfig channelConfig = (new Gson()).fromJson(configs, StartChannelConfig.class);
                if (channelConfig == null || channelConfig.isAnyValueMissing()) {
                    logger.error("Failed to parse channel configs - " + configs);
                    return createResponse(Status.BAD_REQUEST, "Must provide source, target and filter at the json body" + configs);
                }
                source = channelConfig.getSource();
                target = channelConfig.getTarget();
                jsonFilter = channelConfig.getFilter().toString();
            } else if (isNullOrEmpty(source) || isNullOrEmpty(target) || isNullOrEmpty(jsonFilter)) {
                return createResponse(Status.BAD_REQUEST, "Must provide source, target and filter params as json body or query params");
            }

            logger.info(String.format("Received POST command on start-new with params source=%s, target=%s, filter=%s", source, target, jsonFilter));

            UUID channelId = UUID.randomUUID();
            logger.info("Starting new channel with id - " + channelId);

            String topicName = Helper.generateOutputTopicName(channelId);

            KafkaHelper.createNewTopic(topicName);
            logger.info("Output topic for the channel was created successfully - " + topicName);

            dbManager.addNewChannel(channelId, source, target, jsonFilter);
            logger.info("Successfully inserted new filter into the DB");

            return createResponse(Status.OK, channelId.toString());
        } catch (Exception e) {
            logger.error("Error during start of a new channel", e);
            return createResponse(Status.INTERNAL_SERVER_ERROR, "ERROR !!! " + e.toString());
        }
    }

    private class StartChannelConfig {
        private final String source;
        private final String target;
        private final JsonObject filter;

        StartChannelConfig(String source, String target, JsonObject filter) {
            this.source = source;
            this.target = target;
            this.filter = filter;
        }

        String getSource() {
            return source;
        }

        String getTarget() {
            return target;
        }

        JsonObject getFilter() {
            return filter;
        }

        boolean isAnyValueMissing() {
            return isNullOrEmpty(source) || isNullOrEmpty(target) || (filter == null);
        }
    }
}
