package net.souchay.swift.gui.dnd;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class GlobalExecutorService {

    private final static Logger LOG = Logger.getLogger("swift.executor"); //$NON-NLS-1$

    private static AtomicInteger references = new AtomicInteger(0);

    /**
     * Get the Executor service
     * 
     * @return The global executor service
     */
    public static synchronized ScheduledExecutorService getExecutorService() {
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(16, new ThreadFactory() {

                private AtomicInteger i = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "globalExecutorService" + i.incrementAndGet() + "/" + new Date()); //$NON-NLS-1$//$NON-NLS-2$
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        return executor;
    }

    /**
     * Decrements the references
     * 
     * @return
     */
    public static int incrementReferences() {
        return references.incrementAndGet();
    }

    /**
     * Decrements the references
     * 
     * @return
     */
    public static int decrementReferences() {
        int v = references.decrementAndGet();
        if (v < 1) {
            synchronized (GlobalExecutorService.class) {
                if (executor == null)
                    return v;
                executor.shutdown();
                LOG.fine("No more executor references, requesting shutdown..."); //$NON-NLS-1$
                if (v < 0) {
                    v = 0;
                    references.set(0);
                }
                try {
                    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        LOG.warning("Executor still has some unfinished tasks"); //$NON-NLS-1$
                    } else {
                        LOG.info("Executor Terminated"); //$NON-NLS-1$
                        System.exit(0);
                    }
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
                executor = null;
            }
        }
        return v;
    }

    private static ScheduledExecutorService executor;

}
