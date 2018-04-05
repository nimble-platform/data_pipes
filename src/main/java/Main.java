import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Created by evgeniyh on 3/29/18.
 */

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    private static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";

    static String TABLE_NAME = "data_pipes_demo";

    private static String INPUT_TOPIC = "streams-input";

    private static String FILTERED_TOPIC = "streams-filtered";
    private static String NON_FILTERED_TOPIC = "streams-non-filtered";

    //    TODO: validate arguments mutual exclusive
//    TODO: replace the loggers
    public static void main(String[] args) {

        ArgumentParser parser = getArgumentParser();
        try {
            Namespace ns = parser.parseArgs(args);
            String consumerType = ns.getString("start_consumer");
            if (consumerType != null) {
                String topic = consumerType.equals("filtered") ? FILTERED_TOPIC : NON_FILTERED_TOPIC;
                Properties consumerProperties = getProperties("consumer.properties");
                ConsumerRunnable consumerRunnable = new ConsumerRunnable(consumerProperties, topic);
                consumerRunnable.run();
            } else if (ns.getBoolean("create_data_pipe_db")) {
                DBManager dbManager = new DBManager(TABLE_NAME);
                String dataJson = createNewDataJson();
                UUID uuid = dbManager.addNewDataPipeFilter(dataJson);
                System.out.print(uuid);
            } else if (ns.getBoolean("start_streams")) {

                updateJaasConfiguration();

//                String json = (new DBManager(TABLE_NAME)).getDataPipeJson(UUID.fromString(uuid));
//                JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);

//                DataPipeFilter filter = new DataPipeFilter(
//                        jsonObject.get("from").getAsLong(),
//                        jsonObject.get("to").getAsLong(),
//                        jsonObject.get("machineId").getAsString());

                final StreamsBuilder builder = new StreamsBuilder();
                KStream<String, String> stream = builder.stream(INPUT_TOPIC);

                FiltersManager fm = new FiltersManager();
                stream.filter(fm).to(FILTERED_TOPIC);
                stream.filter(new InvertedFilter(fm)).to(NON_FILTERED_TOPIC);

                final Topology topology = builder.build();
                Properties props = getProperties("streams.properties");

                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                final KafkaStreams streams = new KafkaStreams(topology, props);
                final CountDownLatch latch = new CountDownLatch(1);

                streams.start();
                latch.await();
            } else if (ns.getBoolean("start_producer")) {
                String uuid = ns.getString("uuid");
                if (uuid == null) {
                    throw new IllegalArgumentException("Missing the uuid argument for the producer");
                }
                Properties producerProperties = getProperties("producer.properties");
                ProducerRunnable producerRunnable = new ProducerRunnable(producerProperties, INPUT_TOPIC, UUID.fromString(uuid));
                producerRunnable.run();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static String createNewDataJson() {
        long current = System.currentTimeMillis();
        long currentPlusTwoDays = current + 1000 * 60 * 60 * 24; // 86,400,000

        String machineId = getRandomMachineId();
        String[] targets = {"user_test@provernis.com"};

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", current);
        jsonObject.addProperty("to", currentPlusTwoDays);
        jsonObject.addProperty("machineId", machineId);

        JsonArray jsonArray = new JsonArray();
        for (String target : targets) {
            jsonArray.add(target);
        }
        jsonObject.add("targets", jsonArray);

        return (new Gson()).toJson(jsonObject);
    }

    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newFor("Data-Pipes CLI")
                .build()
                .defaultHelp(true)
                .description("Command to run the streams demo");
        parser.addArgument("--start-consumer")
                .choices("filtered", "non-filtered")
                .help("Starts specific consumer");
        parser.addArgument("--start-producer")
                .action(Arguments.storeTrue())
                .help("Starts the random generating producer");
        parser.addArgument("--start-streams")
                .action(Arguments.storeTrue())
                .help("Starts the filtering of the input topic");
        parser.addArgument("--create-data-pipe-db")
                .action(Arguments.storeTrue())
                .help("Will create a new data pipe in db");
        parser.addArgument("--uuid")
                .help("The id of the stored data pipe json in db");

        return parser;
    }

    private static Properties getProperties(String fileName) throws Exception {
        Properties result = new Properties();
        try (InputStream propsStream = Main.class.getClassLoader().getResourceAsStream(fileName)) {
            result.load(propsStream);
        }
        return result;
    }


    public static void updateJaasConfiguration() throws IOException {
        String credentials = System.getenv("MESSAGE_HUB_CREDENTIALS");
        if (credentials == null) {
            logger.error("Failed to get message hub credentials - exiting");
            System.exit(1);
        }
        JsonObject jsonObject = (JsonObject) (new JsonParser().parse(credentials));
        String apiKey  = jsonObject.get("api_key").getAsString();

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Failed to initialise api key");
            throw new RuntimeException("Unable to set the topics handler");
        } else {
            logger.debug("Admin url and api-key were set successfully");
        }

        String username = apiKey.substring(0, 17);
        String password = apiKey.substring(17);

        String jaasConfPath = System.getProperty("java.io.tmpdir") + File.separator + "jaas.conf";
        System.setProperty(JAAS_CONFIG_PROPERTY, jaasConfPath);

        InputStream template = Main.class.getClassLoader().getResourceAsStream("jaas.conf.template");
        String jaasTemplate = new BufferedReader(new InputStreamReader(template)).lines().parallel().collect(Collectors.joining("\n"));

        try (OutputStream jaasOutStream = new FileOutputStream(jaasConfPath, false)) {
            String fileContents = jaasTemplate
                    .replace("$USERNAME", username)
                    .replace("$PASSWORD", password);

            jaasOutStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
        }
    }

    public static String generateNewData(UUID filterId) {
//        JsonObject jsonObject = new JsonObject();

        String machineId = getRandomMachineId();

        ProducedData newData = new ProducedData(System.currentTimeMillis(), machineId, "This is random data from " + machineId, filterId);

//        jsonObject.addProperty("machineId", machineId);
//        jsonObject.addProperty("time", System.currentTimeMillis());
//        jsonObject.addProperty("data", "This is random data from " + machineId);

        return (new Gson()).toJson(newData);
    }

    private static String getRandomMachineId() {
        return String.format("machine_id_%d", (new Random()).nextInt(4));
    }
}
