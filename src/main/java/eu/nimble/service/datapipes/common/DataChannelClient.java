package eu.nimble.service.datapipes.common;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DataChannel client
 *
 * @author Andrea Musumeci
 */
@Service
public class DataChannelClient {

    @Value("${nimble.datachannel.datachannel-url}")
    private String dataChannelServicee;

    private static Logger logger = LoggerFactory.getLogger(DataChannelClient.class);

    public boolean isAuthorized(
            String channelID,
            Long sensorID,
            String bearer)
        throws UnirestException {
        boolean isAuthorized = false;

        String dataChannelUrl = dataChannelServicee+"/channel/isAuthorized";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", bearer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("channelID", channelID);
        if (sensorID!=null && sensorID!=-1) {
            fields.put("sensorID", sensorID);
        }

        HttpResponse<String> response = Unirest.post(dataChannelUrl).headers( headers ).fields(fields).asString();

        isAuthorized = response.getStatus() == 200;
        return isAuthorized;
}

}
