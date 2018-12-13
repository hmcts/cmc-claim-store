package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyElement;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

@Component
public class ContactDetailsMapper implements BuilderMapper<CCDClaimant, ContactDetails, CCDClaimant.CCDClaimantBuilder> {

    @Override
    public void to(ContactDetails contactDetails, CCDClaimant.CCDClaimantBuilder builder) {

        contactDetails.getEmail().ifPresent(builder::representativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::representativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::representativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDClaimant ccdClaimant) {
        if (ccdClaimant == null) {
            return null;
        }

        return new ContactDetails(
            ccdClaimant.getRepresentativeOrganisationPhone(),
            ccdClaimant.getRepresentativeOrganisationEmail(),
            ccdClaimant.getRepresentativeOrganisationDxAddress()
        );


    }
}
