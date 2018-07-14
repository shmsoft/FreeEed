package org.freeeed.main.processinginvoker;

import org.freeeed.main.EmlFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EmailProcessInvoker {

    private static BlockingQueue<EmailProcessingArg> queue = new LinkedBlockingQueue<>();
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProcessInvoker.class);

    static {
        for (int i = 1; i <= MAX_THREADS; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        EmailProcessingArg emailProcessingArg = queue.take();
                        LOGGER.info("RECEIVED ARG TO CALL EMAIL PROCESSING " + emailProcessingArg + " REMAINING " + queue.size());
                        EmlFileProcessor fileProcessor = new EmlFileProcessor(emailProcessingArg.getEmailDir(), emailProcessingArg.getMetadataWriter(), emailProcessingArg.getLuceneIndex());
                        fileProcessor.process(emailProcessingArg.hasAttachments(), emailProcessingArg.getHash());
                    }
                } catch (Exception ex) {
                    LOGGER.error("ERROR: ", ex);
                }
            });
            thread.setName("THREAD_" + i);
            thread.start();
        }
    }

    public static void queueEmailProcessingArg(EmailProcessingArg arg) {
        queue.add(arg);
        LOGGER.info("CURRENT QUEUE SIZE : " + queue.size());
    }

    public static boolean isQueueEmpty() {
        return queue.isEmpty();
    }
}
