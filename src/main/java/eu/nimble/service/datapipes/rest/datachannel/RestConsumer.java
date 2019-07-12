package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;
import static eu.nimble.service.datapipes.common.Helper.isNullOrEmpty;
import java.util.Collections;
import java.util.Properties;

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
            @RequestParam("maxwaitms") int maxwaitms, 
            @ApiParam(name = "maxbytes", value = "Max bytes to receive default 52428800", required = false)
            @RequestParam("maxbytes") int maxbytes, 
            @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
            @RequestHeader(value = "Authorization") String bearer
    ) {
        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized
        
        String userID = "user";// + System.currentTimeMillis();
        String companyID = "company";// + System.currentTimeMillis();

        if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor) ) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);
        //Configurations.CONSUMER_PROPERTIES $$set id User
        Properties consProp = (Properties) Configurations.CONSUMER_PROPERTIES.clone();
        //this enable all users in the same company to receive same messages
        consProp.setProperty("group.id", companyID+"."+userID);
        consProp.setProperty("client.id", companyID+"."+userID);
        if (maxbytes == 0) maxbytes = 52428800;
        
        consProp.setProperty("fetch_max_bytes", maxbytes+"");////default 52428800
        
        
        kafkaConsumer = new KafkaConsumer<>(consProp);
        kafkaConsumer.subscribe(Collections.singletonList(topicName));

        
        if (maxwaitms ==0) maxwaitms=5000;
         ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(maxwaitms);
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
    
}