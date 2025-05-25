package com.alerts.decorator;

import com.alerts.IAlert;
import com.alerts.PatientRecord;

public abstract class AlertDecorator implements IAlert {
    protected final IAlert decoratedAlert;

    public AlertDecorator(IAlert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("Alert cannot be null");
        }
        this.decoratedAlert = alert;
    }

    @Override
    public int getPatientId() {
        return decoratedAlert.getPatientId();
    }

    @Override
    public PatientRecord getVitals() {
        return decoratedAlert.getVitals();
    }

    @Override
    public long getTimestamp() {
        return decoratedAlert.getTimestamp();
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