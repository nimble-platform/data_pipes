package eu.nimble.service.datapipes.rest.datachannel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.nimble.service.datapipes.common.Configurations;
import eu.nimble.service.datapipes.common.Helper;
import eu.nimble.service.datapipes.kafka.KafkaHelper;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


import static eu.nimble.service.datapipes.common.Helper.isNullOrEmpty;
import static eu.nimble.service.datapipes.rest.check.DataPipeCheckService.dbManager;
import org.springframework.http.HttpStatus;

/**
 * Created by evgeniyh on 5/8/18
 * Modified by andrea.musumeci on 07.05.10
 */

@Controller
@RequestMapping(path = "/manage")
@Api("Data Pipes service manager API")
public class DataPipesDatachannelManager implements DataPipesDatachannelManagerApi {
    private static final Logger logger = Logger.getLogger(DataPipesDatachannelManager.class);
    private Gson gson = new Gson();

    public ResponseEntity<?> startNewChannel(
                @ApiParam(value = "configs", required = true) @RequestParam String configs,
                @ApiParam(value = "source", required = true) @RequestParam String source,
                @ApiParam(value = "target", required = true) @RequestParam String target,
                @ApiParam(value = "jsonFilter", required = true) @RequestParam String jsonFilter,
                @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                @RequestHeader(value = "Authorization") String bearer
    )
   {
        try {
            if (!Configurations.enableStream) {
                        return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            if (!isNullOrEmpty(configs)) {
                StartChannelConfig channelConfig = gson.fromJson(configs, StartChannelConfig.class);
                if (channelConfig == null || channelConfig.isAnyValueMissing()) {
                    logger.error("Failed to parse channel configs - " + configs);
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                source = channelConfig.getSource();
                target = channelConfig.getTarget();
                jsonFilter = channelConfig.getFilter().toString();
            } else if (isNullOrEmpty(source) || isNullOrEmpty(target) || isNullOrEmpty(jsonFilter)) {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }

            logger.info(String.format("Received POST command on start-new with params source=%s, target=%s, filter=%s", source, target, jsonFilter));

            UUID channelId = UUID.randomUUID();
            logger.info("Starting new channel with id - " + channelId);

            String topicName = Helper.generateOutputTopicName(channelId);

            KafkaHelper.createNewTopic(topicName);
            logger.info("Output topic for the channel was created successfully - " + topicName);

            dbManager.addNewChannel(channelId, source, target, jsonFilter);
            logger.info("Successfully inserted new filter into the DB");

            CreateChannelResponse response = new CreateChannelResponse();
            response.setChannelId( channelId.toString() );
            response.setInputTopic(Configurations.STREAMS_INPUT_TOPIC);
            response.setOutputTopic(topicName);
            return new ResponseEntity(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error during start of a new channel", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    
    public ResponseEntity<?> createInternalSensorTopic(
                @ApiParam(value = "idDataChannel", required = true) @RequestParam String idDataChannel,
                @ApiParam(value = "idSensor", required = true) @RequestParam String idSensor,
                @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                @RequestHeader(value = "Authorization") String bearer
    ) {
        try {
            
            
        // check if request is authorized $$TODO
        //will ask to datachannelservice if user is authorized to work on this channel and sensor
            

            if ( isNullOrEmpty(idDataChannel)  ||  isNullOrEmpty(idSensor)  ) {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }

            logger.info(String.format("Received POST command on createInternalDataChannel with params idDatachannel=%s, idSensor=%s ", idDataChannel, idSensor));


            String topicName = Helper.generateInternalTopicName(idDataChannel, idSensor);
            logger.info("Starting new internal topic - " + topicName);

            KafkaHelper.createNewTopic(topicName);
            
            CreateChannelResponse response = new CreateChannelResponse();
            response.setChannelId( topicName );
            response.setInputTopic(topicName);
            response.setOutputTopic(topicName);
            return new ResponseEntity(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error during start of a new internal channel", e);
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    
    
    private class StartChannelConfig {
        private final String source;
        private final String target;
        private final JsonObject filter;

        StartChannelConfig(String source, String target, JsonObject filter) {
            this.source = source;
            this.target = target;
            this.filter = filter;
        }

        String getSource() {
            return source;
        }

        String getTarget() {
            return target;
        }

        JsonObject getFilter() {
            return filter;
        }

        boolean isAnyValueMissing() {
            return isNullOrEmpty(source) || isNullOrEmpty(target) || (filter == null);
        }
    }
}
