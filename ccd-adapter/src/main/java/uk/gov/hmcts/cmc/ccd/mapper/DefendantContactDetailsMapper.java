package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DefendantContactDetailsMapper
    implements BuilderMapper<CCDDefendant, ContactDetails, CCDDefendant.CCDDefendantBuilder> {

    @Override
    public void to(ContactDetails contactDetails, CCDDefendant.CCDDefendantBuilder builder) {

        contactDetails.getEmail().ifPresent(builder::claimantProvidedRepresentativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::claimantProvidedRepresentativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::claimantProvidedRepresentativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDDefendant ccdDefendant) {
        if (isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationEmail())
            && ccdDefendant.getClaimantProvidedRepresentativeOrganisationDxAddress() == null
        ) {
            return null;
        }

        return ContactDetails.builder()
            .phone(ccdDefendant.getClaimantProvidedRepresentativeOrganisationPhone())
            .email(ccdDefendant.getClaimantProvidedRepresentativeOrganisationEmail())
            .dxAddress(ccdDefendant.getClaimantProvidedRepresentativeOrganisationDxAddress())
            .build();
    }
}
