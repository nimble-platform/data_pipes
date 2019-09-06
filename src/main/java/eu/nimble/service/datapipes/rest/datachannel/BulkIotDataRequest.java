package eu.nimble.service.datapipes.rest.datachannel;

import java.util.ArrayList;

public class BulkIotDataRequest {
    private String idDataChannel;
    private String idSensor;
    private ArrayList<IotData> bulkIotData;


    public String getIdDataChannel() {
        return idDataChannel;
    }

    public void setIdDataChannel(String idDataChannel) {
        this.idDataChannel = idDataChannel;
    }

    public String getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(String idSensor) {
        this.idSensor = idSensor;
    }

    public ArrayList<IotData> getBulkIotData() {
        return bulkIotData;
    }

    public void setBulkIotData(ArrayList<IotData> bulkIotData) {
        this.bulkIotData = bulkIotData;
    }
}
