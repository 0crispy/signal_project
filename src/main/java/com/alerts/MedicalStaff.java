package com.alerts;

import java.util.Set;

public class MedicalStaff {
    private int id;
    private String name;
    private Set<Integer> assignedPatients;
    
    /**
     * Called when an alert is received.
     * @param alert the alert
     */
    public void receiveAlert(Alert alert){
        // TODO: handle alert
    }
    
    /**
     * Checks if this staff is subscribed to a patient.
     * @param id the patient ID
     * @return true if subscribed, false otherwise
     */
    public boolean isSubscribedToPatient(int id){
        return assignedPatients.contains(id);
    }
}
