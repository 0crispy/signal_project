package com.alerts.thresholds;

import com.alerts.PatientRecord;

public interface VitalsThreshold {
    public boolean checkThreshold(PatientRecord data);
}
