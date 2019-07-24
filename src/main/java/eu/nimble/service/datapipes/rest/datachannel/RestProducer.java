package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;
import static eu.nimble.service.datapipes.common.Helper.createResponse;
import static eu.nimble.service.datapipes.common.Helper.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Optional;
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

import eu.nimble.service.datapipes.kafka.KafkaHelper;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

    public RestProducer() {
        super();
    }

    public ResponseEntity<?> sendIotData(
            @ApiParam(name = "idDataChannel", value = "", required = true)
            @RequestParam("idDataChannel") String idDataChannel, 
            @ApiParam(name = "idSensor", value = "", required = true)
            @RequestParam("idSensor") String idSensor, 
            @ApiParam(name = "datakey", value = "Primary key", required = false)
            @RequestParam("datakey") Optional<String> datakey,
            @ApiParam(name = "iotData", value = "json iot Data", required = true)
            @RequestParam("iotData") String iotData,
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
        ) {

        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        String companyID = "company";

        //this enable all users in the same company to receive same messages
        Properties prodProp = (Properties) Configurations.PRODUCER_PROPERTIES.clone();
        //this would enable all users in the same company to receive same messages
        //prodProp.setProperty("client.id", companyID+"."+userID);
        //prodProp.setProperty("group.id", companyID+"."+userID);

        prodProp.setProperty("client.id", companyID);

        kafkaProducer = new KafkaProducer<>(prodProp);
        
        
        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor)  ||  isNullOrEmpty(iotData)  ) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);

        logger.info(String.format("Sending '%s' to topic '%s'", iotData, topicName));
        try {
            String key = "";
            if (datakey.isPresent()) key = datakey.get();

            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, iotData);

            //Future<RecordMetadata> future =
            kafkaProducer.send(record);

            //RecordMetadata metadata = future.get(100, TimeUnit.MILLISECONDS);

            logger.info(String.format("Message successfully sent,topic=%s iotData=%s", topicName, iotData));
        } catch (Exception e) {
            logger.error("Failed to send message to topic - " + topicName + " "+iotData, e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        kafkaProducer.close();
        return new ResponseEntity(HttpStatus.OK);
        }

    public ResponseEntity<?> sendBulkIotData(
            @ApiParam(value = "Bulk iotData request", required = true)
            @RequestBody BulkIotDataRequest bulkIotDataRequest,
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
    ) {

        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        //String userID = "nimbleuser";
        String companyID = "company";

        //this enable all users in the same company to receive same messages
        Properties prodProp = (Properties) Configurations.PRODUCER_PROPERTIES.clone();
        //this enable all users in the same company to receive same messages
        //prodProp.setProperty("group.id", companyID+"."+userID);
        prodProp.setProperty("client.id", companyID);

        /* in order to manage hight amount of bulk data this are the property which can be modified; in this Nimble instance we use default values
        *
        * batch.size //default 16384; if set to 0 batch is disabled
        * linger.ms //default 0
        *
        * for example
        *
        * //Linger up to 100 ms before sending batch if size not met
        prodProp.put(ProducerConfig.LINGER_MS_CONFIG, 100);

        //Batch up to 64K buffer sizes.
        prodProp.put(ProducerConfig.BATCH_SIZE_CONFIG,  16_384 * 4);

        **/


        kafkaProducer = new KafkaProducer<>(prodProp);


        if ( isNullOrEmpty(bulkIotDataRequest.getIdDataChannel())  ||  isNullOrEmpty(bulkIotDataRequest.getIdSensor()) ) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String topicName = Helper.generateInternalTopicName(bulkIotDataRequest.getIdDataChannel(), bulkIotDataRequest.getIdSensor());

        try {
            for (IotData iotDatum : bulkIotDataRequest.getIotData()) {

                if ( isNullOrEmpty(iotDatum.getDatakey())  ||  isNullOrEmpty(iotDatum.getIotData())  ) {
                    //ignore the bulk line
                } else {
                    logger.info(String.format("Sending '%s' to topic '%s'", iotDatum.getDatakey(), topicName));
                        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, iotDatum.getDatakey(), iotDatum.getIotData());
                        //Future<RecordMetadata> future =
                        kafkaProducer.send(record);
                        //RecordMetadata metadata = future.get(100, TimeUnit.MILLISECONDS);
                        logger.info(String.format("Message successfully sent,topic=%s message=%s", topicName, iotDatum.getIotData()));
                }
                }

        } catch (Exception e) {
            logger.error("Failed to send message to topic - " + topicName, e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        kafkaProducer.close();
        return new ResponseEntity(HttpStatus.OK);
    }



}
