package filters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.log4j.Logger;
import rest.Main;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static common.Configurations.CHANNEL_ID_KEY;

/**
 * Created by evgeniyh on 4/2/18.
 */

public class FiltersManager implements Predicate<String, String> {
    private final static Logger logger = Logger.getLogger(FiltersManager.class);

    private final Map<UUID, ChannelFilter> idToFilter = new ConcurrentHashMap<>();
    private final JsonParser parser = new JsonParser();

    @Override
    public boolean test(String s, String s2) {
        JsonObject object;
        try {
            object = parser.parse(s2).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to parse the message into JSON - " + s2);
            return false;
        }
        String idString = object.get(CHANNEL_ID_KEY).getAsString();
        if (idString == null) {
            logger.error("Failed to read channelId from the message - " + s2);
            return false;
        }

        UUID channelId = UUID.fromString(idString);

        ChannelFilter filter = idToFilter.get(channelId);
        if (filter == null) { // Missing locally - get it from the DB
            logger.info("Missing (locally) channel with id - " + channelId);
            try {
                String storedJson = Main.dbManager.getFilterJson(channelId);
                JsonObject filterJsonObject = parser.parse(storedJson).getAsJsonObject();
                filter = new ChannelFilter(filterJsonObject);

                idToFilter.put(channelId, filter);
            } catch (Exception e) {
                logger.error("Failed to load the filter id from DB - " + channelId, e);
                e.printStackTrace();
                return false;
            }
        }
        return filter.test(object);
    }
}
