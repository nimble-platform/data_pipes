package eu.nimble.service.datapipes.rest.datachannel;


/**
 * datakey - identify the iot data row, usefull for searching in a iot data set. In Product life cycle is normally the serialnumber but can be creation timestamp too.
 * iotData - for product lifecycle management normally it is a string in json format. In order to use filtering service on this message we have choosen some rules: thinking
 * to the json iot Data like as a treview the convention in order to search throght dataset is to put the body of the message in an attribute which label is iotData or jsonData
 * and as first level attributes the ones to be used in search
 *
 * For example if you have theese iotData row
 * datakey: 3939393, iotData: {product : { serialnumber: 3939393, name: 'dishwasher', qualitytest:'passed', producingcycle: '28282836' } }
 * datakey: 3939394, iotData: {product : { serialnumber: 3939394, name: 'dishwasher', qualitytest:'passed', producingcycle: '28282836' } }
 * datakey: 3939395, iotData: {product : { serialnumber: 3939395, name: 'dishwasher', qualitytest:'not-passed', producingcycle: '98787836' } }
 *
 * and you want to enable to search service you will have to change json iotData row as next examples.
 *
 * - by serialnumber you will have this instance for first row done in this way
 * datakey: 3939393
 * iotData: {serialnumber: 3939393, jsonIotData: {product : { serialnumber: 3939393, name: 'dishwasher', qualitytest:'passed', producingcycle: '28282836' } } }
 *
 * - by producingcycle
 * datakey: 3939393
 * iotData: {producingcycle: '28282836', jsonIotData: {product : { serialnumber: 3939393, name: 'dishwasher', qualitytest:'passed', producingcycle: '28282836' } } }
 *
 * - all rows by qualitytest or the rows by serialnumber
 * datakey: 3939393
 * iotData: {serialnumber: 3939393, qualitytest:'passed', jsonIotData: {product : { serialnumber: 3939393, name: 'dishwasher', qualitytest:'passed', producingcycle: '28282836' } } }
 *
**/


public class IotData {
    private String datakey="";
    private String iotData=null;

    public String getDatakey() {
        return datakey;
    }

    public void setDatakey(String datakey) {
        this.datakey = datakey;
    }


    public String getIotData() {
        return iotData;
    }

    public void setIotData(String iotData) {
        this.iotData = iotData;
    }
}
