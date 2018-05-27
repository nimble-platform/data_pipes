package kafka;

import common.Helper;
import common.RESTRequest;
import org.apache.log4j.Logger;

import static common.Configurations.MESSAGE_HUB_CREDENTIALS;
import static common.Helper.startNewRunnable;

/**
 * Created by evgeniyh on 5/24/18.
 */

public class KafkaHelper {
    private final static Logger logger = Logger.getLogger(KafkaHelper.class);

    public KafkaHelper() {

        RESTRequest restApi = new RESTRequest(MESSAGE_HUB_CREDENTIALS.getKafka_rest_url(), MESSAGE_HUB_CREDENTIALS.getApi_key());
        try {
            String topics = restApi.get("/admin/topics", false);
//            System.out.println("The topics are : " + topics);
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void createNewTopic(String topic) {
        logger.debug(String.format("Trying to create topic '%s'", topic));

        try {
            RESTRequest restApi = new RESTRequest(MESSAGE_HUB_CREDENTIALS.getKafka_rest_url(), MESSAGE_HUB_CREDENTIALS.getApi_key());

            // Create a topic, ignore a 422 response - this means that the
            // topic name already exists.
//            System.out.println("AASSDD " + new CreateTopicParameters(topic).toString());
            String postResult = restApi.post("/admin/topics", new CreateTopicParameters(topic).toString(), new int[]{422});

            logger.info(String.format("Topic named '%s' was created with POST result - '%s'", topic, postResult));
//            TODO: add set of existing topics
        } catch (Exception e) {
            logger.error(String.format("Exception on creating topic '%s' ", topic), e);
        }
    }
}
