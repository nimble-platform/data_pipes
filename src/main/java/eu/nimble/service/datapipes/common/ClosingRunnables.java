package eu.nimble.service.datapipes.common;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by evgeniyh on 5/24/18.
 */

class ClosingRunnables implements Runnable {
    private final static Logger logger = Logger.getLogger(ClosingRunnables.class);

    private List<Closeable> closeable = new ArrayList<>();

    void addCloseable(Closeable c) {
        closeable.add(c);
    }

    @Override
    public void run() {
        for (Closeable c : closeable) {
            String className = c.getClass().toString();
            try {
                logger.info("Trying to close - " + className);
                c.close();
            } catch (IOException e) {
                logger.error("Failed to close - " + className, e);
                e.printStackTrace();
            }
        }

    }
}
