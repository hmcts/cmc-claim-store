package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Address;

public class SampleAddress {

    private String line1 = "52";
    private String line2 = "Down Street";
    private String city = "Manchester";
    private String postcode = "DF1 3LJ";

    public static SampleAddress builder() {
        return new SampleAddress();
    }

    public static Address validDefaults() {
        return builder().build();
    }

    public SampleAddress withLine1(String line1) {
        this.line1 = line1;
        return this;
    }

    public SampleAddress withLine2(String line2) {
        this.line2 = line2;
        return this;
    }

    public SampleAddress withCity(String city) {
        this.city = city;
        return this;
    }

    public SampleAddress withPostcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    public Address build() {
        return new Address(line1, line2, city, postcode);
    }
}
