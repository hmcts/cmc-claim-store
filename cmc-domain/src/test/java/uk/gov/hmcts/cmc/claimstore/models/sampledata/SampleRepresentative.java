package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.sampledata.SampleContactDetails;

public class SampleRepresentative {

    private String companyName = "Trading ltd";
    private ContactDetails companyContactDetails = SampleContactDetails.builder().build();
    private Address companyAddress = SampleAddress.validDefaults();

    public static SampleRepresentative builder() {
        return new SampleRepresentative();
    }

    public SampleRepresentative withContactDetails(ContactDetails companyContactDetails) {
        this.companyContactDetails = companyContactDetails;
        return this;
    }

    public SampleRepresentative withBusinessName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public SampleRepresentative withCompanyAddress(Address companyAddress) {
        this.companyAddress = companyAddress;
        return this;
    }

    public Representative build() {
        return new Representative(companyName, companyAddress, companyContactDetails);
    }
}
