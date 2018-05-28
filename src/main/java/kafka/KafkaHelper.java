package kafka;

import com.google.gson.Gson;
import common.Helper;
import common.RESTRequest;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static common.Configurations.MESSAGE_HUB_CREDENTIALS;
import static common.Helper.startNewRunnable;

/**
 * Created by evgeniyh on 5/24/18.
 */

public class KafkaHelper {
    private final static Logger logger = Logger.getLogger(KafkaHelper.class);

    private static Set<String> existingTopics;

    static {
        logger.info("Initialising the existing list of topics");
        RESTRequest restApi = new RESTRequest(MESSAGE_HUB_CREDENTIALS.getKafka_rest_url(), MESSAGE_HUB_CREDENTIALS.getApi_key());
        try {
            String topicsResponse = restApi.get("/admin/topics", false);
            ExistingTopic[] topics = (new Gson()).fromJson(topicsResponse, ExistingTopic[].class);

            existingTopics = Arrays.stream(topics).map(ExistingTopic::getName).collect(Collectors.toSet());
            logger.info("The existing topics are : " + String.join(",", existingTopics));
        } catch (Exception e) {
            logger.error("Error during load of the existing topics", e);
        }
    }

    public static void startKafkaStreams() {
        KafkaStreamsRunnable r = new KafkaStreamsRunnable();
        startNewRunnable(r, "Kafka Streams Runnable");
        Helper.addCloseableToShutdownHook(r);
    }

    public static void startDbLoggerKafkaConsumer() {
        DBLoggerKafkaConsumer consumer = new DBLoggerKafkaConsumer();
        startNewRunnable(consumer, "Kafka DB Logger Consumer");
        Helper.addCloseableToShutdownHook(consumer);
    }

    public static boolean isTopicExists(String topic) {
        return existingTopics.contains(topic);
    }

    public static void createNewTopic(String topic) {
        logger.debug(String.format("Trying to create topic '%s'", topic));

        if (existingTopics.contains(topic)) {
            logger.info(topic + " already exists");
            return;
        }
        try {
            RESTRequest restApi = new RESTRequest(MESSAGE_HUB_CREDENTIALS.getKafka_rest_url(), MESSAGE_HUB_CREDENTIALS.getApi_key());

            // Create a topic, ignore a 422 response - this means that the topic name already exists.
            String postResult = restApi.post("/admin/topics", new CreateTopicParameters(topic).toString(), new int[]{422});

            logger.info(String.format("Topic named '%s' was created with POST result - '%s'", topic, postResult));
            existingTopics.add(topic);
        } catch (Exception e) {
            logger.error(String.format("Exception on creating topic '%s' ", topic), e);
        }
    }

    private class ExistingTopic {
        private final String name;
        private final int partitions;
        private final long retentionMs;
        private final String cleanupPolicy;
        private final boolean markedForDeletion;

        ExistingTopic(String name, int partitions, long retentionMs, String cleanupPolicy, boolean markedForDeletion) {
            this.name = name;
            this.partitions = partitions;
            this.retentionMs = retentionMs;
            this.cleanupPolicy = cleanupPolicy;
            this.markedForDeletion = markedForDeletion;
        }

        public String getName() {
            return name;
        }

        public int getPartitions() {
            return partitions;
        }

        public long getRetentionMs() {
            return retentionMs;
        }

        public String getCleanupPolicy() {
            return cleanupPolicy;
        }

        public boolean isMarkedForDeletion() {
            return markedForDeletion;
        }
    }
}
