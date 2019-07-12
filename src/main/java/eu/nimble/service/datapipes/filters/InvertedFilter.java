package eu.nimble.service.datapipes.filters;

import org.apache.kafka.streams.kstream.Predicate;

/**
 * Created by evgeniyh on 4/1/18.
 */

public class InvertedFilter implements Predicate<String, String> {
    private final Predicate<String, String> filter;

    public InvertedFilter(Predicate<String, String> filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(String s, String s2) {
        return !filter.test(s, s2);
    }
}
