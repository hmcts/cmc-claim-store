package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

@Component
public class DefendantContactDetailsMapper implements BuilderMapper<CCDDefendant, ContactDetails, CCDDefendant.CCDDefendantBuilder> {

    @Override
    public void to(ContactDetails contactDetails, CCDDefendant.CCDDefendantBuilder builder) {

        contactDetails.getEmail().ifPresent(builder::representativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::representativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::representativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDDefendant ccdDefendant) {
        if (ccdDefendant == null) {
            return null;
        }

        return new ContactDetails(
            ccdDefendant.getRepresentativeOrganisationPhone(),
            ccdDefendant.getRepresentativeOrganisationEmail(),
            ccdDefendant.getRepresentativeOrganisationDxAddress()
        );


    }
}
