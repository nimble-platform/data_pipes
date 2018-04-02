import com.google.gson.Gson;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by evgeniyh on 4/2/18.
 */

public class FiltersManager implements Predicate<String, String> {
    private final static Logger logger = Logger.getLogger(FiltersManager.class);

    private Map<UUID, DataPipeFilter> idToFilter = new HashMap<>();

    public FiltersManager() {

    }

    @Override
    public boolean test(String s, String s2) {
        ProducedData data = (new Gson()).fromJson(s2, ProducedData.class);

        UUID filterId = data.getFilterId();
        if (filterId == null) { // TODO: delete - for demo with the old data
            return false;
        }

        DataPipeFilter filter = idToFilter.get(data.getFilterId());
        if (filter == null) { // Missing locally - get it from the DB
            filterId = data.getFilterId();
            logger.info("Missing filter with id - " + filterId);
            try {
                DBManager dbManager = new DBManager(Main.TABLE_NAME);
                String storedJson = dbManager.getDataPipeJson(filterId);
                FilterConfigurations fc = (new Gson()).fromJson(storedJson, FilterConfigurations.class);
                filter = new DataPipeFilter(fc);
                idToFilter.put(filterId, filter);
            } catch (Exception e) {
                logger.error("Failed to load the filter id from DB - " + filterId);
                e.printStackTrace();
                return false;
            }
        }
        return filter.test(s, s2);
    }
}
