package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.rest.check.*;
import java.io.IOException;
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
        @RequestMapping(value = "/getNextMessages", produces = {"application/json"}, method = RequestMethod.POST)
        ResponseEntity<?> getNextMessages(
                                    @ApiParam(value = "idDataChannel", required = true)
                                    @RequestParam String idDataChannel,
                                    @ApiParam(value = "idSensor", required = true)
                                    @RequestParam String idSensor,
                                    @ApiParam(value = "maxwaitms", required = false)
                                    @RequestParam int maxwaitms,
                                    @ApiParam(value = "maxbytes", required = false)
                                    @RequestParam int maxbytes,
                                    @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                                    @RequestHeader(value = "Authorization") String bearer)
         throws IOException, UnirestException;

    }