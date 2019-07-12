package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.rest.check.*;
import java.io.IOException;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @Api(value = "channel", description = "the DataPipes Datachannel Producer iot Data API")
    public interface DataPipesDatachannelProducerApi {

        @ApiOperation(value = "Send iot data", notes = "Send iot data")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Send iot data"),
                @ApiResponse(code = 400, message = "Bad request")
            }
        )
        @RequestMapping(value = "/sendIotData", produces = {"application/json"}, method = RequestMethod.POST)
        ResponseEntity<?> sendIotData(
                                    @ApiParam(value = "idDataChannel", required = true)
                                    @RequestParam String idDataChannel,
                                    @ApiParam(value = "idSensor", required = true)
                                    @RequestParam String idSensor,
                                    @ApiParam(value = "datakey", required = true)
                                    @RequestParam String datakey,
                                    @ApiParam(value = "iotData", required = true)
                                    @RequestParam String iotData,
                                    @ApiParam(name = "Authorization", value = "OpenID Connect token containing identity of requester", required = true)
                                    @RequestHeader(value = "Authorization") String bearer)
         throws IOException, UnirestException;
    }