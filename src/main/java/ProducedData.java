import java.util.UUID;

/**
 * Created by evgeniyh on 4/2/18.
 */

public class ProducedData {
    private final long time;
    private final String machineId;
    private final String data;
    private final UUID filterId;

    public ProducedData(long time, String machineId, String data, UUID filterId) {
        this.time = time;
        this.machineId = machineId;
        this.data = data;
        this.filterId = filterId;
    }

    public long getTime() {
        return time;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getData() {
        return data;
    }

    public UUID getFilterId() {
        return filterId;
    }
}
