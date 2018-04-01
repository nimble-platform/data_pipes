import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.log4j.Logger;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class DataPipeFilter implements Predicate<String, String> {
    private final static Logger logger = Logger.getLogger(DataPipeFilter.class);

    private final long from;
    private final long to;
    private final String machineId;

    public DataPipeFilter(long from, long to, String machineId) {
        this.from = from;
        this.to = to;
        this.machineId = machineId;
    }

    @Override
    public boolean test(String key, String value) {
        JsonObject jsonObject = (JsonObject) (new JsonParser()).parse(value);

        String source = jsonObject.get("source").getAsString();
        if (!source.equals(machineId)) {
            logger.info(String.format("The data was received from %s and not from requested %s", source, machineId));
            return false;
        }
        long time = jsonObject.get("time").getAsLong();
        if (time > to) {
            logger.info("The received data has timestamp bigger then allowed");
            return false;
        }
        if (time < from) {
            logger.info("The received data has timestamp smaller then allowed");
            return false;
        }

        return true;
    }
}
