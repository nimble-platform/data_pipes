package eu.nimble.service.datapipes.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class ChannelFilter {
    private final static Logger logger = Logger.getLogger(ChannelFilter.class);
    private final HashMap<String, String> filterMap;

    public ChannelFilter(JsonObject filterObject) {
        filterMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : filterObject.entrySet()) {
            logger.info("Setting filter param (key-value) " + entry.getKey() + "-" + entry.getValue().toString());
            filterMap.put(entry.getKey(), entry.getValue().toString());
        }
    }

    public boolean test(JsonObject data) {
        for (Map.Entry<String, String> filterParam : filterMap.entrySet()) {
            String k = filterParam.getKey();
            String v = filterParam.getValue();


            JsonElement element = data.get(k);
            if (element == null) {
                logger.error("The message doesn't contains the required key - " + k);
                return false;
            }
            if (!element.toString().equals(v)) {
                logger.error(String.format("The key '%s' in message equals '%s' and not as required '%s'", k, element.toString(), v));
                return false;
            }
        }

        logger.info("The message complies with all the required filter params");
        return true;
    }
}
