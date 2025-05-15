package com.alerts.thresholds;

import java.util.ArrayList;
import java.util.List;

public class PatientThresholdProfile {
    private List<VitalsThreshold> thresholds = new ArrayList<VitalsThreshold>();
    
    public ArrayList<VitalsThreshold> getThresholds(){
        return new ArrayList<VitalsThreshold>(thresholds);
    }
}
