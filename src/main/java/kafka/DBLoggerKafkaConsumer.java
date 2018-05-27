package kafka;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.Configurations;
import common.Helper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rest.Main;

import java.io.Closeable;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static common.Configurations.CHANNEL_ID_KEY;
import static common.Helper.isNullOrEmpty;

/**
 * Created by evgeniyh on 5/24/18.
 */

class DBLoggerKafkaConsumer implements Runnable, Closeable {
    private final static Logger logger = Logger.getLogger(DBLoggerKafkaConsumer.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaProducer<String, String> kafkaProducer;

    private final JsonParser parser = new JsonParser();


    DBLoggerKafkaConsumer() {
        logger.info("Starting the consumer on topic - " + Configurations.STREAMS_OUTPUT_TOPIC);

        kafkaConsumer = new KafkaConsumer<>(Configurations.CONSUMER_PROPERTIES);
        kafkaConsumer.subscribe(Collections.singletonList(Configurations.STREAMS_OUTPUT_TOPIC));

        kafkaProducer = new KafkaProducer<>(Configurations.PRODUCER_PROPERTIES);
    }

    @Override
    public void run() {
        logger.info(DBLoggerKafkaConsumer.class.toString() + " is starting");

        while (true) {
            try {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(4000);

                if (records.isEmpty()) {
                    logger.info("No messages consumed - will retry again with 4 second timeout");
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        String message = record.value();

                        JsonObject object = parser.parse(message).getAsJsonObject();
                        String idString = object.get(CHANNEL_ID_KEY).getAsString();
                        UUID channelId = UUID.fromString(idString);

                        if (isNullOrEmpty(idString)) {
                            throw new IllegalArgumentException("ERROR !!! Failed to read channelId from the message - " + message);
                        }
                        trySendMessageToDb(channelId, message);
                        String actualTopicName = Helper.generateOutputTopicName(channelId);
                        //TODO: Create cache of existing topics - avoid call to create

                        sendMessageToActualTopic(actualTopicName, message);
                    } catch (Exception e) {
                        logger.error("Error during parsing message, sending message to topic or DB", e);
                    }

                    logger.info("Sending the message to the output topic");
                }
            } catch (final WakeupException e) {
                logger.log(Level.WARN, "Consumer closing - caught exception: " + e);
            } catch (final KafkaException e) {
                logger.log(Level.ERROR, "Sleeping for 5s - Consumer has caught: " + e, e);
                try {
                    Thread.sleep(5000); // Longer sleep before retrying
                } catch (InterruptedException e1) {
                    logger.error("Error during long sleep losing - caught exception: " + e);
                }
            }
        }
    }

    private void sendMessageToActualTopic(String topic, String message) {
        try {
//            Helper.executeHttpPost(Configurations.CSB_CREATE_TOPIC_URL + topic, true, true);
            // TODO: check for existing topics
            KafkaHelper.createNewTopic(topic);

            logger.info("Sending the message to topic - " + topic + message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key", message);

            Future<RecordMetadata> future = kafkaProducer.send(record);

            RecordMetadata metadata = future.get(5000, TimeUnit.MILLISECONDS);

            logger.info(String.format("Message successfully sent,topic=%s offset=%s, message=%s", topic, metadata.offset(), message));
        } catch (Exception e) {
            logger.error("Failed to send message to topic - " + topic + message, e);
        }
    }

    private void trySendMessageToDb(UUID channelId, String message) {
        try {
            logger.info("Sending the message to the db - " + message);
            Main.dbManager.addNewData(channelId, message);
            logger.info("Message was sent successfully to the DB" + message);
        } catch (Exception e) {
            logger.error("Failed to the send the message to the DB - " + message, e);
        }
    }

    @Override
    public void close() {
        logger.info("Closing producer and consumer");
        try {
            kafkaProducer.close();
            kafkaConsumer.close();
        } catch (Exception e) {
            logger.error("Error during close of producer and consumer");
        }
    }
}
