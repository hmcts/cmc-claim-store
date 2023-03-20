package uk.gov.hmcts.cmc.domain.constraints.utils;

public class PostcodeDistrict {

    String scotlandPostcode;
    String northernIrelandPostcode;

    public String getScotlandPostcode() {
        return scotlandPostcode;
    }

    public void setScotlandPostcode(String scotlandPostcode) {
        this.scotlandPostcode = scotlandPostcode;
    }

    public String getNorthernIrelandPostcode() {
        return northernIrelandPostcode;
    }

    public void setNorthernIrelandPostcode(String northernIrelandPostcode) {
        this.northernIrelandPostcode = northernIrelandPostcode;
    }
}
