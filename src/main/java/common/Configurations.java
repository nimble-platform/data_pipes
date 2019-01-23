package common;

import com.google.gson.Gson;
import kafka.KafkaHelper;
import kafka.MessageHubCredentials;
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
    public static int TOPICS_PARTITIONS;

    public static MessageHubCredentials MESSAGE_HUB_CREDENTIALS;

    static {
        try {
            //ENVIRONMENT = System.getenv("DATA_PIPES_ENVIRONMENT");
            ENVIRONMENT = System.getenv("ENVIRONMENT");
            if (isNullOrEmpty(ENVIRONMENT)) {
                throw new RuntimeException("Missing the ENVIRONMENT variable");
            }

            Properties prop = Helper.loadPropertiesFromResource(ENVIRONMENT + ".properties");
            DATA_TABLE = prop.getProperty("dataTable");
            CHANNELS_TABLE = prop.getProperty("channelsTable");
            OUTPUT_TOPIC_PREFIX = prop.getProperty("topicsPrefix");
            STREAMS_INPUT_TOPIC = prop.getProperty("streamsInputTopic");
            STREAMS_OUTPUT_TOPIC = prop.getProperty("streamsOutputTopic");
            OUTPUT_TOPIC_CONSUMER_ID = prop.getProperty("outputTopicConsumerId");
            STREAMS_APPLICATION_ID = prop.getProperty("streamsApplicationId");
            TOPICS_PARTITIONS = Integer.valueOf(prop.getProperty("topicsPartitions"));

            CONSUMER_PROPERTIES = Helper.loadPropertiesFromResource("consumer.properties");
            CONSUMER_PROPERTIES.put(Configurations.KAFKA_CLIENT_ID, Configurations.OUTPUT_TOPIC_CONSUMER_ID);

            STREAMS_PROPERTIES = Helper.loadPropertiesFromResource("streams.properties");
            STREAMS_PROPERTIES.put("application.id", STREAMS_APPLICATION_ID);
            STREAMS_PROPERTIES.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            STREAMS_PROPERTIES.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

            PRODUCER_PROPERTIES = Helper.loadPropertiesFromResource("producer.properties");

            String credentials = System.getenv("MESSAGE_HUB_CREDENTIALS");
            if (isNullOrEmpty(credentials)) {
                throw  new RuntimeException("Failed to load message hub credentials");
            }
            MESSAGE_HUB_CREDENTIALS = (new Gson()).fromJson(credentials, MessageHubCredentials.class);
            Helper.updateJaasConfiguration(MESSAGE_HUB_CREDENTIALS.getUser(), MESSAGE_HUB_CREDENTIALS.getPassword());

            logger.info("Verifying the streams input and output topics exists");
            KafkaHelper.createNewTopic(STREAMS_INPUT_TOPIC);
            KafkaHelper.createNewTopic(STREAMS_OUTPUT_TOPIC);
        } catch (Exception e) {
            logger.error("Error during load of the configurations", e);
            System.exit(1);
        }
    }
}
