package eu.nimble.service.datapipes.rest.datachannel;

import java.util.ArrayList;

public class BulkIotDataRequest {
    private String idDataChannel;
    private String idSensor;
    private ArrayList<IotData> iotData;


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

    public ArrayList<IotData> getIotData() {
        return iotData;
    }

    public void setIotData(ArrayList<IotData> iotData) {
        this.iotData = iotData;
    }
}
