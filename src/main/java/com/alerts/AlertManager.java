package com.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlertManager {
    private List<Alert> alertLog = new ArrayList<>();
    private Map<Integer, MedicalStaff> staff = new HashMap<Integer, MedicalStaff>();

    public void handleAlert(Alert alert){
        for (MedicalStaff staffMember : staff.values()) {
            if (staffMember.isSubscribedToPatient(alert.getPatientId())){
                staffMember.receiveAlert(alert);
            }
        }
        alertLog.add(alert);
    }
    public ArrayList<Alert> getAlertLog(){
        return new ArrayList<Alert>(alertLog);
    }
}
