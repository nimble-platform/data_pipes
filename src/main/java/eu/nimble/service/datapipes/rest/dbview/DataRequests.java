package eu.nimble.service.datapipes.rest.dbview;

import eu.nimble.service.datapipes.rest.check.DataPipeCheckService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.nimble.service.datapipes.common.Channel;
import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;
import eu.nimble.service.datapipes.kafka.KafkaHelper;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by evgeniyh on 5/14/18.
 */

@Controller
@RequestMapping(path = "/datarequest/")
@Api("Data Pipes request datarequest API")
public class DataRequests {
    private final static Logger logger = Logger.getLogger(DataRequests.class);

    private Gson gson = new Gson();

    @GET
    @Path("/{target}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllChannelsForTarget(@PathParam("target") String target) {
        if (!Configurations.enableDbPersistence) {
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Service not enabled to get data for target - " + target);
        }
        if (Helper.isNullOrEmpty(target)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Target name can't be null or empty");
        }
        try {
            logger.info("Retrieving all the channels for target - " + target);

            ResultSet rs = DataPipeCheckService.dbManager.getChannelsForTarget(target);
            JsonObject returnObject = createJsonObjectWithJsonArrayFromResult(rs, "channels");

            return Helper.createResponse(Response.Status.OK, gson.toJson(returnObject));
        } catch (Exception e) {
            logger.error("Failed to read channels for target - " + target, e);
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to get data for target - " + target);
        }
    }


    @GET
    @Path("/{channelId}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMessageForChannel(@PathParam("channelId") String channelId) {
        if (!Configurations.enableDbPersistence) {
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Service not enabled");
        }
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            logger.info("Retrieving all the messages for channel id - " + channelId);
            UUID channelUuid = UUID.fromString(channelId);
            ResultSet rs = DataPipeCheckService.dbManager.getDataForChannelId(channelUuid);
            JsonObject returnObject = createJsonObjectWithJsonArrayFromResult(rs, "messages");

            return Helper.createResponse(Response.Status.OK, gson.toJson(returnObject));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse the channel id - " + channelId, e);
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Wrong channel id (should be UUID)");
        } catch (Exception e) {
            logger.error("Failed to read messages for channel id - " + channelId, e);
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to get messages for channel - " + channelId);
        }
    }


    @GET
    @Path("/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannel(@PathParam("channelId") String channelId) {
        if (!Configurations.enableDbPersistence) {
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Service not enabled");
        }
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            logger.info("Retrieving from DB channel with id - " + channelId);
            UUID channelUuid = UUID.fromString(channelId);
            Channel channel = DataPipeCheckService.dbManager.getChannel(channelUuid);

            return Helper.createResponse(Response.Status.OK, new Gson().toJson(channel));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse the channel id - " + channelId, e);
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Wrong channel id (should be UUID)");
        } catch (Exception e) {
            logger.error("Failed to read messages for channel id - " + channelId, e);
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to get messages for channel - " + channelId);
        }
    }

    @DELETE
    @Path("/{channelId}")
    public Response deleteChannel(@PathParam("channelId") String channelId) {
        if (!Configurations.enableDbPersistence) {
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Service not enabled");
        }
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            UUID channelUuid = UUID.fromString(channelId);

            logger.info("At channels table deleting channel with id - " + channelId);
            DataPipeCheckService.dbManager.deleteChannel(channelUuid);

            logger.info("At data table deleting all the message for channel id - " + channelId);
            DataPipeCheckService.dbManager.deleteMessages(channelUuid);

            String topicToDelete = Helper.generateOutputTopicName(channelUuid);
            KafkaHelper.deleteTopic(topicToDelete);

            return Helper.createResponse(Response.Status.OK, "Successfully deleted filter and messages for channel id - " + channelId);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse the channel id - " + channelId, e);
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Wrong channel id (should be UUID)");
        } catch (Exception e) {
            logger.error("Error during the deletion of channel with id - " + channelId, e);
            return Helper.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error during deletion of channel - " + channelId);
        }
    }

    private JsonObject createJsonObjectWithJsonArrayFromResult(ResultSet rs, String arrayName) throws SQLException {
        JsonArray array = new JsonArray();
        if (!rs.isBeforeFirst()) {
            logger.error("Array = " + arrayName + " has no values from the query result set");
        }
        while (rs.next()) {
            String msg = rs.getString(1);
            array.add(msg);
        }
        JsonObject object = new JsonObject();
        object.add(arrayName, array);

        return object;
    }
}