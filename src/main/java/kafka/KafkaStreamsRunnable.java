package kafka;

import common.Configurations;
import filters.FiltersManager;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by evgeniyh on 5/24/18.
 */

class KafkaStreamsRunnable implements Runnable, Closeable {
    private final static Logger logger = Logger.getLogger(KafkaStreamsRunnable.class);

    private KafkaStreams streams;

    KafkaStreamsRunnable() {
        logger.info("Creating the DB Logger kafka consumer for the output topic");
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder.stream(Configurations.STREAMS_INPUT_TOPIC);

        stream.filter(new FiltersManager()).to(Configurations.STREAMS_OUTPUT_TOPIC);

        final Topology topology = builder.build();

        streams = new KafkaStreams(topology, Configurations.STREAMS_PROPERTIES);
    }

    @Override
    public void run() {
        logger.info("Starting the DB Logger kafka consumer thread");
        streams.start();
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing kafka streams");
        if (streams != null) {
            streams.close();
        }
    }
}