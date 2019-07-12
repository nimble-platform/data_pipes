package eu.nimble.service.datapipes.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nimble.service.datapipes.common.Configurations;

/**
 * Created by evgeniyh on 5/25/18.
 */


public class CreateTopicParameters {

    @JsonProperty("name")
    private final String topicName;

    @JsonProperty("partitions")
    private final int partitionCount = Configurations.TOPICS_PARTITIONS;

    @JsonProperty("configs")
    private final CreateTopicConfig config = new CreateTopicConfig();

    @JsonCreator
    public CreateTopicParameters(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public CreateTopicConfig getConfig() {
        return config;
    }

    /**
     * Convert an instance of this class to its JSON string representation.
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this);
        } catch (final JsonProcessingException e) {
            return "";
        }
    }

    private static class CreateTopicConfig {
        private static final long _24H_IN_MILLISECONDS = 3600000L * 24;

        @JsonProperty("retentionMs")
        private static long retentionMs = _24H_IN_MILLISECONDS;

        public long getRetentionMs() {
            return retentionMs;
        }
    }
}
