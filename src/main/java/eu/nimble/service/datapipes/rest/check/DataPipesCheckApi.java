package eu.nimble.service.datapipes.rest.check;

import java.io.IOException;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.ws.rs.core.Response;

    @Api(value = "channel", description = "the DataPipes API")
    public interface DataPipesCheckApi {

        @ApiOperation(value = "Hello message", notes = "Hello message")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Hello message", response = ResponseCheck.class)
            }
        )
        @RequestMapping(value = "/hello", produces = {"application/json"}, method = RequestMethod.GET)
        ResponseEntity<?> getHello()
                throws IOException, UnirestException;

        @ApiOperation(value = "runHealthCheck", notes = "runHealthCheck")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "runHealthCheck", response = ResponseCheck.class) })
        @RequestMapping(value = "/health-check", produces = {"application/json"}, method = RequestMethod.GET)
        ResponseEntity<?> runHealthCheck()
                throws IOException, UnirestException;

        /**
         * See API documentation
         *
         * @return Response Hello
         * @throws UnirestException Error while communication with the Identity Service
         */
        @ApiOperation(value = "runDBReconnect", notes = "runDBReconnect",
                response = String.class, nickname = "runDBReconnect")
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "runDBReconnect", response = ResponseCheck.class) })
        @RequestMapping(value = "/reconnect", produces = {"application/json"}, method = RequestMethod.GET)
        ResponseEntity<?> runDBReconnect()
                throws IOException, UnirestException;

    }