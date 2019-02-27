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

        contactDetails.getEmail().ifPresent(builder::applicantProvidedRepresentativeOrganisationEmail);
        contactDetails.getPhone().ifPresent(builder::applicantProvidedRepresentativeOrganisationPhone);
        contactDetails.getDxAddress().ifPresent(builder::applicantProvidedRepresentativeOrganisationDxAddress);
    }

    @Override
    public ContactDetails from(CCDRespondent ccdRespondent) {
        if (isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationEmail())
            && ccdRespondent.getApplicantProvidedRepresentativeOrganisationDxAddress() == null
        ) {
            return null;
        }

        return ContactDetails.builder()
            .phone(ccdRespondent.getApplicantProvidedRepresentativeOrganisationPhone())
            .email(ccdRespondent.getApplicantProvidedRepresentativeOrganisationEmail())
            .dxAddress(ccdRespondent.getApplicantProvidedRepresentativeOrganisationDxAddress())
            .build();
    }
}
