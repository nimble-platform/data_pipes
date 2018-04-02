/**
 * Created by evgeniyh on 4/2/18.
 */

public class FilterConfigurations {
    private final long from;
    private final long to;
    private final String machineId;

    public FilterConfigurations(long from, long to, String machineId) {
        this.from = from;
        this.to = to;
        this.machineId = machineId;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public String getMachineId() {
        return machineId;
    }
}
