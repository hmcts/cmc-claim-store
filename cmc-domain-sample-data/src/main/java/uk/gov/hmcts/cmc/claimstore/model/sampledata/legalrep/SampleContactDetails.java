package uk.gov.hmcts.cmc.claimstore.model.sampledata.legalrep;

import uk.gov.hmcts.cmc.claimstore.models.legalrep.ContactDetails;

public class SampleContactDetails {

    private String phone = "7873738547";
    private String email = "representative@example.org";
    private String dxNumber = "DX123456";

    public static SampleContactDetails builder() {
        return new SampleContactDetails();
    }

    public static ContactDetails validDefaults() {
        return builder().build();
    }

    public SampleContactDetails withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public SampleContactDetails withEmail(String email) {
        this.email = email;
        return this;
    }

    public SampleContactDetails withDxNumber(String dxNumber) {
        this.dxNumber = dxNumber;
        return this;
    }

    public ContactDetails build() {
        return new ContactDetails(phone, email, dxNumber);
    }

}
