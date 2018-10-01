package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative.RepresentativeBuilder;
import uk.gov.hmcts.cmc.domain.models.sampledata.legalrep.SampleContactDetails;

public class SampleRepresentative {

    private SampleRepresentative() {
        super();
    }

    public static RepresentativeBuilder builder() {
        return Representative.builder()
            .organisationName("Trading ltd")
            .organisationAddress(SampleAddress.builder().build())
            .organisationContactDetails(SampleContactDetails.builder().build());
    }
}
