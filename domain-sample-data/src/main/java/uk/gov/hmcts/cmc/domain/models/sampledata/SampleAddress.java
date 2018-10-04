package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Address.AddressBuilder;

public class SampleAddress {

    private SampleAddress() {
        super();
    }

    public static AddressBuilder builder() {
        return Address.builder()
            .line1("52")
            .line2("Down Street")
            .line3("Salford")
            .city("Manchester")
            .postcode("DF1 3LJ");
    }
}
