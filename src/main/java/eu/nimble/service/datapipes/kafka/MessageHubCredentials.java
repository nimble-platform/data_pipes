package eu.nimble.service.datapipes.kafka;

/**
 * Created by evgeniyh on 5/27/18.
 */

public class MessageHubCredentials {
    private final String instance_id;
    private final String mqlight_lookup_url;
    private final String api_key;
    private final String kafka_admin_url;
    private final String kafka_rest_url;
    private final String[] kafka_brokers_sasl;
    private final String user;
    private final String password;

    public MessageHubCredentials(String instance_id, String mqlight_lookup_url, String api_key, String kafka_admin_url,
                                 String kafka_rest_url, String[] kafka_brokers_sasl, String user, String password) {
        this.instance_id = instance_id;
        this.mqlight_lookup_url = mqlight_lookup_url;
        this.api_key = api_key;
        this.kafka_admin_url = kafka_admin_url;
        this.kafka_rest_url = kafka_rest_url;
        this.kafka_brokers_sasl = kafka_brokers_sasl;
        this.user = user;
        this.password = password;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public String getMqlight_lookup_url() {
        return mqlight_lookup_url;
    }

    public String getApi_key() {
        return api_key;
    }

    public String getKafka_admin_url() {
        return kafka_admin_url;
    }

    public String getKafka_rest_url() {
        return kafka_rest_url;
    }

    public String[] getKafka_brokers_sasl() {
        return kafka_brokers_sasl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
