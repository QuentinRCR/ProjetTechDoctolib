package com.docto.protechdoctolib.communicationMeans;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CommunicationMeans {

    @Id
    private String communicationMean;


    public CommunicationMeans(String communicationMean) {
        this.communicationMean = communicationMean;
    }

    public CommunicationMeans() {
    }

    public String getCommunicationMean() {
        return communicationMean;
    }

    public void setCommunicationMean(String communicationMean) {
        this.communicationMean = communicationMean;
    }
}
