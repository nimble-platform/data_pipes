package eu.nimble.service.datapipes.rest.datachannel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.nimble.service.datapipes.common.Channel;
import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;

import static eu.nimble.service.datapipes.common.Configurations.CHANNEL_ID_KEY;
import static eu.nimble.service.datapipes.common.Helper.isNullOrEmpty;

import java.util.*;

import eu.nimble.service.datapipes.filters.ChannelFilter;
import eu.nimble.service.datapipes.rest.check.DataPipeCheckService;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by andrea.musumeci
 */

@Controller
@RequestMapping(path = "/consumer")
@Api("Data Pipes service consumer API")
public class RestConsumer  implements DataPipesDatachannelConsumerApi {
    private final static Logger logger = Logger.getLogger(RestConsumer.class);

    private KafkaConsumer<String, String> kafkaConsumer;

    public ResponseEntity<?> getNextMessages(
            @ApiParam(name = "idDataChannel", value = "", required = true)
            @RequestParam("idDataChannel") String idDataChannel,
            @ApiParam(name = "idSensor", value = "", required = true)
            @RequestParam("idSensor") String idSensor,
            @ApiParam(name = "maxwaitms", value = "Max ms to wait - default 5000", required = false)
            @RequestParam("maxwaitms")Optional<Integer> maxwaitms,
            @ApiParam(name = "maxbytes", value = "Max bytes to receive default 52428800", required = false)
            @RequestParam("maxbytes") Optional<Integer> maxbytes,
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
    ) {

        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        //String userID = "nimbleuser"; //if it will be necessary to give to each user his messages list this has to be checked with identity service; at this moment tail is organized at company level
        String companyID = "company";

        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor) ) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);
        //Configurations.CONSUMER_PROPERTIES $$set id User
        Properties consProp = (Properties) Configurations.CONSUMER_PROPERTIES.clone();
        //this enable all users in the same company to receive same messages
        //consProp.setProperty("group.id", companyID+"."+userID);
        //consProp.setProperty("client.id", companyID+"."+userID);
        //if two users of same company are consuming iotData this will receive them in roundrobin.
        consProp.setProperty("client.id", companyID);

        int bytes = 52428800;
        if (maxbytes.isPresent()) bytes = maxbytes.get().intValue();
        consProp.setProperty("fetch_max_bytes", bytes+"");
        
        kafkaConsumer = new KafkaConsumer<>(consProp);
        kafkaConsumer.subscribe(Collections.singletonList(topicName));

        int wait = 5000;
        if (maxwaitms.isPresent()) wait = maxwaitms.get().intValue();

         ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(wait);
         ResponseConsumeNextMessages messages = new ResponseConsumeNextMessages();
         consumerRecords.forEach(record -> {
            System.out.println("Record Key " + record.key());
              System.out.println("Record value " + record.value());
              messages.addMessage(record.value());
              System.out.println("Record partition " + record.partition());
              System.out.println("Record offset " + record.offset());
           });

        logger.info(String.format("Reading sensor '%s', idDataChannel '%s', topic '%s', messages '%s', ", idSensor, idDataChannel, topicName,""+messages.getMessages().size()));
        kafkaConsumer.close();
        
        return new ResponseEntity(messages, HttpStatus.OK);
    }


    public ResponseEntity<?> getFilteredMessages(
            @ApiParam(name = "idDataChannel", value = "", required = true)
            @RequestParam("idDataChannel") String idDataChannel,
            @ApiParam(name = "idSensor", value = "", required = true)
            @RequestParam("idSensor") String idSensor,
            @ApiParam(name = "filterField", value = "field name of filter", required = true)
            @RequestParam("filterField") String filterField,
            @ApiParam(name = "filterValue", value = "simple filter to be applied server side", required = true)
            @RequestParam("filterValue") String filterValue,
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
    ) {

        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        //String userID = "nimbleuser"; //if it will be necessary to give to each user his messages list this has to be checked with identity service; at this moment tail is organized at company level
        String companyID = "company";

        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor) ) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);
        Properties consProp = (Properties) Configurations.CONSUMER_PROPERTIES.clone();
        consProp.setProperty("client.id", companyID+".basicfilter."+System.currentTimeMillis());

        kafkaConsumer = new KafkaConsumer<>(consProp);
        kafkaConsumer.subscribe(Collections.singletonList(topicName));

        int wait = 5000;

        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(wait);
        ResponseConsumeNextMessages messages = new ResponseConsumeNextMessages();
        consumerRecords.forEach(record -> {
            System.out.println("Record Key " + record.key());
            System.out.println("Record value " + record.value());
            if (verifyBasicFilter(record.value(), filterField,filterValue)  )
                messages.addMessage(record.value());
            System.out.println("Record partition " + record.partition());
            System.out.println("Record offset " + record.offset());
        });

        logger.info(String.format("Reading sensor '%s', idDataChannel '%s', topic '%s', messages '%s', ", idSensor, idDataChannel, topicName,""+messages.getMessages().size()));
        kafkaConsumer.close();

        return new ResponseEntity(messages, HttpStatus.OK);
    }


    JsonParser parser = new JsonParser();

    private boolean verifyBasicFilter(String jsonValue, String k, String v) {
       logger.info(String.format("verifyBasicFilter '%s', jsonValue '%s', key '%s', values '%s' ", jsonValue, k, v));
            JsonObject data = parser.parse(jsonValue).getAsJsonObject();

            JsonElement element = data.get(k);
            if (element == null) {
                logger.error("The message doesn't contains the required key - " + k);
                return false;
            }
            if (!element.toString().equals(v)) {
                logger.error(String.format("The key '%s' in message equals '%s' and not as required '%s'", k, element.toString(), v));
                return false;
            }
            return true;

    }



}