package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
        if (isBlank(ccdDefendant.getRepresentativeOrganisationPhone())
            && isBlank(ccdDefendant.getRepresentativeOrganisationEmail())
            && ccdDefendant.getRepresentativeOrganisationDxAddress() == null
        ) {
            return null;
        }

        return new ContactDetails(
            ccdDefendant.getRepresentativeOrganisationPhone(),
            ccdDefendant.getRepresentativeOrganisationEmail(),
            ccdDefendant.getRepresentativeOrganisationDxAddress()
        );


    }
}
