package rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.Channel;
import common.Helper;
import kafka.KafkaHelper;
import org.apache.log4j.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by evgeniyh on 5/14/18.
 */

@Path("/")
public class DataRequests {
    private final static Logger logger = Logger.getLogger(DataRequests.class);

    private Gson gson = new Gson();

    @GET
    @Path("/{target}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllChannelsForTarget(@PathParam("target") String target) {
        if (Helper.isNullOrEmpty(target)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Target name can't be null or empty");
        }
        try {
            logger.info("Retrieving all the channels for target - " + target);

            ResultSet rs = Main.dbManager.getChannelsForTarget(target);
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
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            logger.info("Retrieving all the messages for channel id - " + channelId);
            UUID channelUuid = UUID.fromString(channelId);
            ResultSet rs = Main.dbManager.getDataForChannelId(channelUuid);
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
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            logger.info("Retrieving from DB channel with id - " + channelId);
            UUID channelUuid = UUID.fromString(channelId);
            Channel channel = Main.dbManager.getChannel(channelUuid);

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
        if (Helper.isNullOrEmpty(channelId)) {
            return Helper.createResponse(Response.Status.BAD_REQUEST, "Channel id can't be null or empty");
        }
        try {
            UUID channelUuid = UUID.fromString(channelId);

            logger.info("At channels table deleting channel with id - " + channelId);
            Main.dbManager.deleteChannel(channelUuid);

            logger.info("At data table deleting all the message for channel id - " + channelId);
            Main.dbManager.deleteMessages(channelUuid);

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
