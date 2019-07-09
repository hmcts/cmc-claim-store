package uk.gov.hmcts.cmc.ccd_adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ContactDetailsMapper
    implements BuilderMapper<CCDApplicant, ContactDetails, CCDApplicant.CCDApplicantBuilder> {

    @Override
    public void to(ContactDetails contactDetails, CCDApplicant.CCDApplicantBuilder builder) {

        contactDetails.getEmail().ifPresent(builder::representativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::representativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::representativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDApplicant ccdApplicant) {
        if (isBlank(ccdApplicant.getRepresentativeOrganisationPhone())
            && isBlank(ccdApplicant.getRepresentativeOrganisationEmail())
            && ccdApplicant.getRepresentativeOrganisationDxAddress() == null
        ) {
            return null;
        }

        return new ContactDetails(
            ccdApplicant.getRepresentativeOrganisationPhone(),
            ccdApplicant.getRepresentativeOrganisationEmail(),
            ccdApplicant.getRepresentativeOrganisationDxAddress()
        );
    }
}
