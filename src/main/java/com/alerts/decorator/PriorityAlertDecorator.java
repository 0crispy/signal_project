package com.alerts.decorator;

import com.alerts.IAlert;

public class PriorityAlertDecorator extends AlertDecorator {
    public enum Priority {
        LOW(0),
        MEDIUM(1),
        HIGH(2),
        CRITICAL(3);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

     private Priority priority;
    private String priorityReason;

    /**
     * Creates a new priority alert decorator.
     *
     * @param alert the alert to decorate
     * @param initialPriority the initial priority level
     * @param reason reason for the priority level
     */
    public PriorityAlertDecorator(IAlert alert, Priority initialPriority, String reason) {
        super(alert);
        if (initialPriority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }

         this.priority = initialPriority;
        this.priorityReason = reason != null ? reason : "No reason specified";
    }

    /**
     * Gets the current priority level.
     * @return the priority level
     */
    public Priority getPriority() {
        return priority;

    }

    /**
     * gets reason for the current priority level
     * @return the priority reason
     */
    public String getPriorityReason() {
        return priorityReason;
    }

    /**
     * updates the priority level with a reason
     * @param newPriority the new priority level
     * @param reason the reason
     */
    public void updatePriority(Priority newPriority, String reason) {
        if (newPriority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.priority = newPriority;
        this.priorityReason = reason != null ? reason : "No reason specified";
    }

    @Override
    public String getAlertType() {
        return decoratedAlert.getAlertType();
    }

    @Override
    public String getDescription() {
        return decoratedAlert.getDescription();
    }
} 