package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DefendantRepresentativeMapper
    implements BuilderMapper<CCDRespondent, Representative, CCDRespondent.CCDRespondentBuilder> {

    private final AddressMapper addressMapper;
    private DefendantContactDetailsMapper defendantContactDetailsMapper;

    @Autowired
    public DefendantRepresentativeMapper(
        AddressMapper addressMapper,
        DefendantContactDetailsMapper defendantContactDetailsMapper
    ) {
        this.addressMapper = addressMapper;
        this.defendantContactDetailsMapper = defendantContactDetailsMapper;
    }

    @Override
    public void to(Representative representative, CCDRespondent.CCDRespondentBuilder builder) {

        representative.getOrganisationContactDetails().ifPresent(organisationContactDetails ->
            defendantContactDetailsMapper.to(organisationContactDetails, builder));

        builder
            .applicantProvidedRepresentativeOrganisationName(representative.getOrganisationName())
            .applicantProvidedRepresentativeOrganisationAddress(
                addressMapper.to(representative.getOrganisationAddress())
            );
    }

    @Override
    public Representative from(CCDRespondent ccdRespondent) {
        if (isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationName())
            && ccdRespondent.getApplicantProvidedRepresentativeOrganisationAddress() == null
            && isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationDxAddress())
            && isBlank(ccdRespondent.getApplicantProvidedRepresentativeOrganisationEmail())
        ) {
            return null;
        }

        return Representative.builder()
            .organisationName(ccdRespondent.getApplicantProvidedRepresentativeOrganisationName())
            .organisationAddress(
                addressMapper.from(ccdRespondent.getApplicantProvidedRepresentativeOrganisationAddress())
            )
            .organisationContactDetails(defendantContactDetailsMapper.from(ccdRespondent))
            .build();

    }
}
