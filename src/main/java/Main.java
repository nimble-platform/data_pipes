import common.Configurations;
import common.Helper;
import db.DBManager;
import kafka.KafkaHelper;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.log4j.Logger;
import runnables.ConsumerRunnable;
import runnables.ProducerRunnable;

import java.util.Set;
import java.util.UUID;

import static common.Configurations.MESSAGE_HUB_CREDENTIALS;

/**
 * Created by evgeniyh on 3/29/18.
 */

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    private static String CONSUMER_COMMAND = "consumer";
    private static String PRODUCER_COMMAND = "producer";
    private static String DELETE_COMMAND = "delete";

    public static void main(String[] args) {
        ArgumentParser parser = getArgumentParser();
        try {
            Namespace ns = parser.parseArgs(args);
            String commandName = ns.getString("command_name");

            Helper.updateJaasConfiguration(MESSAGE_HUB_CREDENTIALS.getUser(), MESSAGE_HUB_CREDENTIALS.getPassword());
            if (commandName.equals(CONSUMER_COMMAND)) {
                String channelIdString = ns.getString("channelId");
                UUID channelId = UUID.fromString(channelIdString);
                ConsumerRunnable consumerRunnable = new ConsumerRunnable(Configurations.CONSUMER_PROPERTIES, channelId);
                consumerRunnable.run();
            } else if (commandName.equals(PRODUCER_COMMAND)) {
                String channelIdString = ns.getString("channelId");
                String consumerCompanyId = ns.getString("consumerCompanyId");
                UUID channelId = UUID.fromString(channelIdString);
                ProducerRunnable producerRunnable = new ProducerRunnable(Configurations.PRODUCER_PROPERTIES, Configurations.STREAMS_INPUT_TOPIC, channelId, consumerCompanyId);
                producerRunnable.run();
            } else if (commandName.equals(DELETE_COMMAND)) {
                if (!Configurations.ENVIRONMENT.equals("dev")) {
                    logger.error("Deleting data is supported only in dev environment");
                    return;
                }
                if (ns.getBoolean("tables")) {
                    DBManager dbManager = new DBManager(Configurations.CHANNELS_TABLE, Configurations.DATA_TABLE);
                    dbManager.deleteTables();

                }
                if (ns.getBoolean("topics")) {
                    System.out.println("Deleting stream's input and output topics");

                    KafkaHelper.deleteTopic(Configurations.STREAMS_OUTPUT_TOPIC);
                    KafkaHelper.deleteTopic(Configurations.STREAMS_INPUT_TOPIC);

                    Set<String> topics = KafkaHelper.getExistingTopics();
                    for (String topic : topics) {
                        if (topic.startsWith(Configurations.OUTPUT_TOPIC_PREFIX)) {
                            System.out.println("Deleting topic - " + topic);
                            KafkaHelper.deleteTopic(topic);
                        }
                    }
                }
            } else {
                System.out.println("Not supported command" + commandName);
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


    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newFor("Runnables CLI")
                .build()
                .defaultHelp(true)
                .description("Command to run the streams demo");
        Subparsers subParser = parser.addSubparsers().dest("command_name");

        ArgumentParser delete = subParser.addParser(DELETE_COMMAND).help("Delete data");
        delete.addArgument("--tables").action(Arguments.storeTrue()).help("Will delete the tables in the database");
        delete.addArgument("--topics").action(Arguments.storeTrue()).help("Will delete all the topics with the dev prefix");

        ArgumentParser consumer = subParser.addParser(CONSUMER_COMMAND).help("Starts consumer");
        consumer.addArgument("--channelId").required(true).help("The channel for which consume from topic");

        ArgumentParser producer = subParser.addParser(PRODUCER_COMMAND).help("Starts producer");
        producer.addArgument("--channelId").required(true).help("The channel id to attach to data");
        producer.addArgument("--consumerCompanyId").required(false).help("The id of the producing company");

        return parser;
    }
}
