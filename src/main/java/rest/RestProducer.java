package rest;

import common.Configurations;
import common.Helper;
import static common.Helper.createResponse;
import static common.Helper.isNullOrEmpty;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import kafka.KafkaHelper;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Created by andrea.musumeci
 */

@Path("/producer")
public class RestProducer extends Application {
    private final static Logger logger = Logger.getLogger(RestProducer.class);
    private KafkaProducer<String, String> kafkaProducer;

    private Gson gson = new Gson();

    public RestProducer() {
        super();
    }

    //    TODO : check for existing topic list (at start)
    @POST
    @Path("/sendInternalIotData")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendInternalDataChannelMessage(
            @QueryParam("idDataChannel") String idDataChannel, 
            @QueryParam("idSensor") String idSensor, 
            @QueryParam("datakey") String datakey,
            @QueryParam("iotData") String iotData,
            @HeaderParam(value = "Authorization") String bearer) {

        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        String userID = "user";
        String companyID = "company";

        //this enable all users in the same company to receive same messages
        Properties prodProp = (Properties) Configurations.PRODUCER_PROPERTIES.clone();
        //this enable all users in the same company to receive same messages
        prodProp.setProperty("group.id", companyID+"."+userID);
        prodProp.setProperty("client.id", companyID+"."+userID);
        
        kafkaProducer = new KafkaProducer<>(prodProp);
        
        
        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor)  ||  isNullOrEmpty(datakey)  ||  isNullOrEmpty(iotData)  ) {
            return createResponse(Response.Status.BAD_REQUEST, "Must provide idDatachannel, idSensor, key, message params not null");
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);

        logger.info(String.format("Sending '%s' to topic '%s'", iotData, topicName));
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, datakey, iotData);

            Future<RecordMetadata> future = kafkaProducer.send(record);

            RecordMetadata metadata = future.get(5000, TimeUnit.MILLISECONDS);

            logger.info(String.format("Message successfully sent,topic=%s offset=%s, message=%s", topicName, metadata.offset(), iotData));
        } catch (Exception e) {
            logger.error("Failed to send message to topic - " + topicName + " "+iotData, e);
        }
        kafkaProducer.close();
        return createResponse(Response.Status.OK, gson.toJson("Message sent"));

    }
}
