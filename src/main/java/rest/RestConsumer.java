package rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.Configurations;
import common.Helper;
import static common.Helper.createResponse;
import static common.Helper.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Created by andrea.musumeci
 */
@Path("/consumer")
public class RestConsumer extends Application {
    private final static Logger logger = Logger.getLogger(RestConsumer.class);

    private KafkaConsumer<String, String> kafkaConsumer;

    private Gson gson = new Gson();

    public RestConsumer() throws Exception {
        super();
    }

    @POST
    @Path("/getNextInternalMessages/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendInternalDataChannelMessage(
            @QueryParam("idDataChannel") String idDataChannel, 
            @QueryParam("idSensor") String idSensor, 
            @QueryParam("maxwaitms") int maxwaitms, 
            @QueryParam("maxbytes") String maxbytes, 
            @HeaderParam(value = "Authorization") String bearer) {

        
        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        
        String userID = "user";// + System.currentTimeMillis();
        String companyID = "company";// + System.currentTimeMillis();

        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor) ) {
            return createResponse(Response.Status.BAD_REQUEST, "Must provide idDatachannel, idSensor params not null");
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);
        //Configurations.CONSUMER_PROPERTIES $$set id User
        Properties consProp = (Properties) Configurations.CONSUMER_PROPERTIES.clone();
        //this enable all users in the same company to receive same messages
        consProp.setProperty("group.id", companyID+"."+userID);
        consProp.setProperty("client.id", companyID+"."+userID);
        consProp.setProperty("fetch_max_bytes", maxbytes);////default 52428800
        
        
        kafkaConsumer = new KafkaConsumer<>(consProp);
        kafkaConsumer.subscribe(Collections.singletonList(topicName));

        
         ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(maxwaitms);
         ArrayList messages = new ArrayList();
         consumerRecords.forEach(record -> {
            System.out.println("Record Key " + record.key());
              System.out.println("Record value " + record.value());
              messages.add(record.value());
              System.out.println("Record partition " + record.partition());
              System.out.println("Record offset " + record.offset());
           });

        logger.info(String.format("Reading sensor '%s', idDataChannel '%s', topic '%s', messages '%s', ", idSensor, idDataChannel, topicName,""+messages.size()));
        kafkaConsumer.close();
        
        JsonObject responseObject = new JsonObject();
        responseObject.add("messages", gson.toJsonTree(messages));
        return createResponse(Response.Status.OK, gson.toJson(responseObject));
    }
    
}