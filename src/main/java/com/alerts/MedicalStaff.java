package com.alerts;

import java.util.Set;

public class MedicalStaff {
    private int id;
    private String name;
    private Set<Integer> assignedPatients;
    
    public void receiveAlert(Alert alert){
        //TODO
    }
    public boolean isSubscribedToPatient(int id){
        return assignedPatients.contains(id);
    }
}
