package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;
import static eu.nimble.service.datapipes.common.Helper.createResponse;
import static eu.nimble.service.datapipes.common.Helper.isNullOrEmpty;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.nimble.service.datapipes.kafka.KafkaHelper;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by andrea.musumeci
 */

@Controller
@RequestMapping(path = "/producer")
@Api("Data Pipes service producer iot Data API")
public class RestProducer implements DataPipesDatachannelProducerApi {
    private final static Logger logger = Logger.getLogger(RestProducer.class);
    private KafkaProducer<String, String> kafkaProducer;

    private Gson gson = new Gson();

    public RestProducer() {
        super();
    }

    public ResponseEntity<?> sendIotData(
            @ApiParam(name = "idDataChannel", value = "", required = true)
            @RequestParam("idDataChannel") String idDataChannel, 
            @ApiParam(name = "idSensor", value = "", required = true)
            @RequestParam("idSensor") String idSensor, 
            @ApiParam(name = "datakey", value = "Primary key", required = false)
            @RequestParam("datakey") String datakey,
            @ApiParam(name = "iotData", value = "json iot Data", required = true)
            @RequestParam("iotData") String iotData,
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
        ) {

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
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        kafkaProducer.close();
        return new ResponseEntity(HttpStatus.OK);
        }
}
