package tourGuide.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.service.TourGuideService;
import tourGuide.model.user.User;

public class Tracker extends Thread {
    private Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    /**
     * this method starts the main thread
     * It uses the CompletableFuture to launch asynchronous tasks
     * its main function is to retrieve the gps position of users asynchronously
     */
    @Override
    public void run() {
        Locale.setDefault(Locale.ENGLISH);
        StopWatch stopWatch = new StopWatch();
        List<CompletableFuture> futures = new ArrayList();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }
            List<User> users = tourGuideService.getAllUsers();
            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            // for each position to retrieve, an asynchronous task is recorded
            users.forEach(u -> futures.add(CompletableFuture.runAsync(() -> tourGuideService.trackUserLocation(u))));
            // we start all the tasks
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                // we are waiting for it all to end
                combinedFuture.get();
            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            } finally {
                stopWatch.stop();
                logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
                stopWatch.reset();
                break;
            }
        }

    }
}
