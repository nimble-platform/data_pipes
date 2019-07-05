package common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
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
import java.util.stream.Collectors;

/**
 * Created by evgeniyh on 5/9/18.
 */

public class Helper {
    private final static Logger logger = Logger.getLogger(Helper.class);

    private static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    private static ClosingRunnables closingRunnables = new ClosingRunnables();

    static {
        logger.info("Adding the closing thread to the shutdown hook");
        Thread closingThread = new Thread(closingRunnables);

        Runtime.getRuntime().addShutdownHook(closingThread);
    }

    public static void startNewRunnable(Runnable runnable, String runnableDescription) {
        try {
            logger.info("Creating new thread for - " + runnableDescription);
            Thread t = new Thread(runnable);
            t.start();
            logger.info("Started successfully thread for - " + runnableDescription);
        } catch (Exception e) {
            logger.error("Error during of thread for - " + runnableDescription, e);
        }
    }

    public static void addCloseableToShutdownHook(Closeable c) {
        logger.info(String.format("Adding object %s to the shutdown hook", c.getClass().toString()));
        closingRunnables.addCloseable(c);
    }

    private static String createNewFilterJson() {
        long current = System.currentTimeMillis();
        long currentPlusTwoDays = current + 1000 * 60 * 60 * 24; // 86,400,000

        String machineId = Helper.generateRandomMachineId();
        String source = "source_user_test@provernis.com";
        String target = "target_user_test@provernis.com";

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", current);
        jsonObject.addProperty("to", currentPlusTwoDays);
        jsonObject.addProperty("machineId", machineId);

        return (new Gson()).toJson(jsonObject);
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static Properties loadPropertiesFromResource(String resourceName) throws IOException {
        try (InputStream is = Helper.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties prop = new Properties();
            prop.load(is);

            return prop;
        }
    }

    public static String generateOutputTopicName(UUID channelId) {
        return Configurations.OUTPUT_TOPIC_PREFIX + channelId;
    }

    public static String generateInternalTopicName(String idDataChannel, String idSensor) {
        return Configurations.INTERNAL_TOPIC_PREFIX+"-" + idDataChannel+"-" + idSensor;
    }

    public static void updateJaasConfiguration(String username, String password) throws IOException {
        if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
            throw new RuntimeException("Message hub username or password can't be empty");
        }

        String jaasConfPath = System.getProperty("java.io.tmpdir") + File.separator + "jaas.conf";
        System.setProperty(JAAS_CONFIG_PROPERTY, jaasConfPath);

        InputStream template = Helper.class.getClassLoader().getResourceAsStream("jaas.conf.template");
        String jaasTemplate = new BufferedReader(new InputStreamReader(template)).lines().parallel().collect(Collectors.joining("\n"));

        try (OutputStream jaasOutStream = new FileOutputStream(jaasConfPath, false)) {
            String fileContents = jaasTemplate
                    .replace("$USERNAME", username)
                    .replace("$PASSWORD", password);

            jaasOutStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
        }
    }

    public static String inputStreamToString(InputStream is) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String executeHttpPost(String url, boolean logResponse, boolean verifyResponseOk) throws Exception {
        HttpPost httpPost = new HttpPost(url);

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpPost)) {

            if (response == null) {
                throw new RuntimeException("http response was null for - " + url);
            }
            if (verifyResponseOk && response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Response wasn't 200");
            }
            String responseString = inputStreamToString(response.getEntity().getContent());
            if (logResponse) {
                logger.info(String.format("Response for url - %s was - %s", url, responseString));
            }
            return responseString;
        } catch (Throwable t) {
            logger.error("Error during execution of POST on - " + url, t);
            throw t;
        }
    }

    public static String generateRandomMachineId() {
        return String.format("machine_id_%d", (new Random()).nextInt(4));
    }

    public static Response createResponse(Response.Status statusCode, String entity) {
        return Response.status(statusCode).entity(entity).build();
    }
}
