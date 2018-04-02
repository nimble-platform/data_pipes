import com.google.gson.Gson;
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

    public DataPipeFilter(FilterConfigurations fc) {
        this(fc.getFrom(), fc.getTo(), fc.getMachineId());
    }

    @Override
    public boolean test(String key, String value) {
        ProducedData data = (new Gson()).fromJson(value, ProducedData.class);

        String sourceMachineId = data.getMachineId();
        if (!sourceMachineId.equals(this.machineId)) {
            logger.info(String.format("The data was received from %s and not from requested %s", sourceMachineId, this.machineId));
            return false;
        }
        long time = data.getTime();
        if (time > to) {
            logger.info("The received data has timestamp bigger then allowed");
            return false;
        }
        if (time < from) {
            logger.info("The received data has timestamp smaller then allowed");
            return false;
        }

        logger.info("The message is complying to the rules - sending to filtered");
        return true;
    }
}
