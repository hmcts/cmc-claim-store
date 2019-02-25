package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DefendantContactDetailsMapper
    implements BuilderMapper<CCDRespondent, ContactDetails, CCDRespondent.CCDRespondentBuilder> {

    @Override
    public void to(ContactDetails contactDetails, CCDRespondent.CCDRespondentBuilder builder) {

        contactDetails.getEmail().ifPresent(builder::claimantProvidedRepresentativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::claimantProvidedRepresentativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::claimantProvidedRepresentativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDRespondent ccdRespondent) {
        if (isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationEmail())
            && ccdRespondent.getClaimantProvidedRepresentativeOrganisationDxAddress() == null
        ) {
            return null;
        }

        return ContactDetails.builder()
            .phone(ccdRespondent.getClaimantProvidedRepresentativeOrganisationPhone())
            .email(ccdRespondent.getClaimantProvidedRepresentativeOrganisationEmail())
            .dxAddress(ccdRespondent.getClaimantProvidedRepresentativeOrganisationDxAddress())
            .build();
    }
}
