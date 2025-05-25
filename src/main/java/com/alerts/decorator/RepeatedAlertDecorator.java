package com.alerts.decorator;

import com.alerts.IAlert;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class RepeatedAlertDecorator extends AlertDecorator {
    private final long repeatInterval;
    private final int maxRepeatCount;
    private final Timer timer;
    //ehh just use this for thread safety
    private final AtomicInteger repeatCount;
    private TimerTask currentTask;

    /**
     * creates a new repeated alert decorator
     *
     * @param alert The alert to decorate
     * @param repeatInterval The interval between repeats in milliseconds
     * @param maxRepeatCount The maximum number of times to repeat the alert (0 for unlimited)
     */
    public RepeatedAlertDecorator(IAlert alert, long repeatInterval, int maxRepeatCount) {
        super(alert);
        if (repeatInterval <= 0) {
            throw new IllegalArgumentException("Repeat interval must be positive");
        }
        if (maxRepeatCount < 0) {
            throw new IllegalArgumentException("Max repeat count cannot be negative");
        }

         this.repeatInterval = repeatInterval;
         this.maxRepeatCount = maxRepeatCount;
         //idk what isDaemon does, but it's cool i think
         this.timer = new Timer(true);
         this.repeatCount = new AtomicInteger(0);
    }

    /**
     * gets the current repeat count
     * @return the number of times this alert has been repeated
     */
    public int getRepeatCount() {
        return repeatCount.get();
    }

    /**
     * Gets the maximum number of repeats allowed
     * @return the maximum repeat count (0 for unlimited)
     */
    public int getMaxRepeatCount() {
        return maxRepeatCount;
    }

    /**
     * Gets the interval between repeats.
     * @return the repeat interval in milliseconds
     */
    public long getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * Starts the alert repetition.
     */
    public void startRepeating() {
        if (currentTask != null) {
            //return if already repeating
            return;
        }

        currentTask = new TimerTask() {
            @Override
            public void run() {
                int count = repeatCount.incrementAndGet();
                if (maxRepeatCount > 0 && count >= maxRepeatCount) {
                    stopRepeating();
                }
            }
        };

        timer.scheduleAtFixedRate(currentTask, repeatInterval, repeatInterval);
    }

    /**
     * stops the alert repetition
     */
    public void stopRepeating() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    @Override
    public String getDescription() {
        return decoratedAlert.getDescription();
    }

    /**
      * Cleans up resources when this decorator is no longer needed.
     */
    public void cleanup() {
        
        stopRepeating();
        timer.cancel();
    }
} 