package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.rest.check.*;
import java.io.IOException;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @Api(value = "channel", description = "the DataPipes Datachannel API")
    public interface DataPipesDatachannelManagerApi {

        @ApiOperation(value = "Start new channel", notes = "Start new channel")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Start new channel", response = CreateChannelResponse.class),
                @ApiResponse(code = 400, message = "Bad request")
            }
        )
        @RequestMapping(value = "/startNewChannel", produces = {"application/json"}, method = RequestMethod.POST)
        ResponseEntity<?> startNewChannel(
                                    @ApiParam(value = "configs", required = true)
                                    @RequestParam String configs,
                                    @ApiParam(value = "source", required = true)
                                    @RequestParam String source,
                                    @ApiParam(value = "taget", required = true)
                                    @RequestParam String target,
                                    @ApiParam(value = "filter", required = true)
                                    @RequestParam String filter,
                                    @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                                    @RequestHeader(value = "Authorization") String bearer)
                throws IOException, UnirestException;


        @ApiOperation(value = "Start new channel", notes = "Start new channel")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Start new channel", response = CreateChannelResponse.class),
                @ApiResponse(code = 400, message = "Bad request")
            }
        )
        @RequestMapping(value = "/createInternalSensorTopic", produces = {"application/json"}, method = RequestMethod.POST)
        ResponseEntity<?> createInternalSensorTopic(
                                    @ApiParam(value = "idDataChannel", required = true)
                                    @RequestParam String idDataChannel,
                                    @ApiParam(value = "idSensor", required = true)
                                    @RequestParam String idSensor,
                                    @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                                    @RequestHeader(value = "Authorization") String bearer)
         throws IOException, UnirestException;

        @ApiOperation(value = "Start new channel", notes = "Start new channel")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Start new channel", response = CreateChannelResponse.class),
                @ApiResponse(code = 400, message = "Bad request")
        }
        )
        @RequestMapping(value = "/createInternalChannelTopic", produces = {"application/json"}, method = RequestMethod.POST)
        ResponseEntity<?> createInternalChannelTopic(
                @ApiParam(value = "idDataChannel", required = true)
                @RequestParam String idDataChannel,
                @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                @RequestHeader(value = "Authorization") String bearer)
                throws IOException, UnirestException;

    }