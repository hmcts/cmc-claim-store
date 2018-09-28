package uk.gov.hmcts.cmc.domain.models.sampledata.legalrep;

import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails.ContactDetailsBuilder;

public class SampleContactDetails {

    private SampleContactDetails() {
        super();
    }

    public static ContactDetailsBuilder builder() {
        return ContactDetails.builder()
            .phone("7873738547")
            .email("representative@example.org")
            .dxAddress("DX123456");
    }
}
