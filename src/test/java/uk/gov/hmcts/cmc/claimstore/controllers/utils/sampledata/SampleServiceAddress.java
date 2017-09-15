package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.ServiceAddress;

public class SampleServiceAddress {

    private String defendantsAddress = "NO";
    private String line1 = "52";
    private String line2 = "Down Street";
    private String city = "Manchester";
    private String postcode = "DF1 3LJ";

    public static SampleServiceAddress builder() {
        return new SampleServiceAddress();
    }

    public static ServiceAddress validDefaults() {
        return builder().build();
    }

    public SampleServiceAddress withDefendantsAddress(final String defendantsAddress) {
        this.defendantsAddress = defendantsAddress;
        return this;
    }

    public SampleServiceAddress withLine1(final String line1) {
        this.line1 = line1;
        return this;
    }

    public SampleServiceAddress withLine2(final String line2) {
        this.line2 = line2;
        return this;
    }

    public SampleServiceAddress withCity(final String city) {
        this.city = city;
        return this;
    }

    public SampleServiceAddress withPostcode(final String postcode) {
        this.postcode = postcode;
        return this;
    }

    public ServiceAddress build() {
        return new ServiceAddress(defendantsAddress, line1, line2, city, postcode);
    }
}
