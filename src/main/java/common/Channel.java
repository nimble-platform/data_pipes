package common;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Created by evgeniyh on 6/21/18.
 */

public class Channel {
    @SerializedName("c_id")
    private final UUID channelId;
    private final String source;
    private final String target;
    private final JsonObject filter;

    public Channel(UUID channelId, String source, String target, JsonObject filter) {
        this.channelId = channelId;
        this.source = source;
        this.target = target;
        this.filter = filter;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public JsonObject getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelId=" + channelId +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", filter=" + filter +
                '}';
    }
}
