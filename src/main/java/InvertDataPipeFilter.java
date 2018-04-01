import org.apache.kafka.streams.kstream.Predicate;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class InvertDataPipeFilter implements Predicate<String, String> {
    private final DataPipeFilter filter;

    public InvertDataPipeFilter(DataPipeFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(String s, String s2) {
        return !filter.test(s, s2);
    }
}
