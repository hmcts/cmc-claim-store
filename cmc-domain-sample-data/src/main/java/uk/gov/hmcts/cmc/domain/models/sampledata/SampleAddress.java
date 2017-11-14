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

    public SampleAddress withLine1(final String line1) {
        this.line1 = line1;
        return this;
    }

    public SampleAddress withLine2(final String line2) {
        this.line2 = line2;
        return this;
    }

    public SampleAddress withCity(final String city) {
        this.city = city;
        return this;
    }

    public SampleAddress withPostcode(final String postcode) {
        this.postcode = postcode;
        return this;
    }

    public Address build() {
        return new Address(line1, line2, city, postcode);
    }
}
