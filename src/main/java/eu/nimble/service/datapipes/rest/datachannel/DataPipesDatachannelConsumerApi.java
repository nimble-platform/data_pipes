package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.rest.check.*;
import java.io.IOException;
import java.util.Optional;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @Api(value = "channel", description = "the DataPipes Datachannel Consumer iot Data API")
    public interface DataPipesDatachannelConsumerApi {

        @ApiOperation(value = "Read next iot data", notes = "Read next iot data")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Read next iot data", response=ResponseConsumeNextMessages.class),
                @ApiResponse(code = 400, message = "Bad request")
            }
        )
        @RequestMapping(value = "/getNextMessages", produces = {"application/json"}, method = RequestMethod.GET)
        ResponseEntity<?> getNextMessages(
                                    @ApiParam(value = "idDataChannel", required = true)
                                    @RequestParam String idDataChannel,
                                    @ApiParam(value = "idSensor", required = true)
                                    @RequestParam String idSensor,
                                    @ApiParam(value = "maxwaitms", required = false)
                                    @RequestParam Optional<Integer> maxwaitms,
                                    @ApiParam(value = "maxbytes", required = false)
                                    @RequestParam Optional<Integer> maxbytes,
                                    @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                                    @RequestHeader(value = "Authorization") String bearer)
         throws IOException, UnirestException;

        @RequestMapping(value = "/getFilteredMessages", produces = {"application/json"}, method = RequestMethod.GET)
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
                @RequestHeader(value = "Authorization") String bearer )
                throws IOException, UnirestException;

    }