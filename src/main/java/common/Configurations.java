package common;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.log4j.Logger;

import java.util.Properties;

import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 5/13/18.
 */

public class Configurations {
    private static final Logger logger = Logger.getLogger(Configurations.class);

    public static String KAFKA_CLIENT_ID = "client.id";

    public static String CHANNEL_ID_KEY = "channelId";

    public static String CSB_CREATE_TOPIC_URL;
    public static String OUTPUT_TOPIC_PREFIX;

    public static String DATA_TABLE;
    public static String CHANNELS_TABLE;

    public static String STREAMS_INPUT_TOPIC;
    public static String STREAMS_OUTPUT_TOPIC;

    public static String OUTPUT_TOPIC_CONSUMER_ID;

    public static Properties CONSUMER_PROPERTIES;
    public static Properties PRODUCER_PROPERTIES;
    public static Properties STREAMS_PROPERTIES;

    public static String ENVIRONMENT;

    public static String STREAMS_APPLICATION_ID;

    static {
        try {
            ENVIRONMENT = System.getenv("ENVIRONMENT");
            if (isNullOrEmpty(ENVIRONMENT)) {
                throw new RuntimeException("Missing the ENVIRONMENT variable");
            }

            Properties prop = Helper.loadPropertiesFromResource(ENVIRONMENT + ".properties");
            DATA_TABLE = prop.getProperty("dataTable");
            CHANNELS_TABLE = prop.getProperty("channelsTable");
            OUTPUT_TOPIC_PREFIX = prop.getProperty("topicsPrefix");
            CSB_CREATE_TOPIC_URL = prop.getProperty("csbCreateTopicPath");
            STREAMS_INPUT_TOPIC = prop.getProperty("streamsInputTopic");
            STREAMS_OUTPUT_TOPIC = prop.getProperty("streamsOutputTopic");
            OUTPUT_TOPIC_CONSUMER_ID = prop.getProperty("outputTopicConsumerId");
            STREAMS_APPLICATION_ID = prop.getProperty("streamsApplicationId");

            logger.info("Verifying the streams input topic exists");

            Helper.executeHttpPost(CSB_CREATE_TOPIC_URL + STREAMS_INPUT_TOPIC, true, true);
            Helper.executeHttpPost(CSB_CREATE_TOPIC_URL + STREAMS_OUTPUT_TOPIC, true, true);

            CONSUMER_PROPERTIES = Helper.loadPropertiesFromResource("consumer.properties");
            CONSUMER_PROPERTIES.put(Configurations.KAFKA_CLIENT_ID, Configurations.OUTPUT_TOPIC_CONSUMER_ID);

            STREAMS_PROPERTIES = Helper.loadPropertiesFromResource("streams.properties");
            STREAMS_PROPERTIES.put("application.id", STREAMS_APPLICATION_ID);
            STREAMS_PROPERTIES.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            STREAMS_PROPERTIES.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

            PRODUCER_PROPERTIES = Helper.loadPropertiesFromResource("producer.properties");
            Helper.updateJaasConfiguration();
        } catch (Exception e) {
            logger.error("Error during load of the configurations", e);
            System.exit(1);
        }
    }
}
