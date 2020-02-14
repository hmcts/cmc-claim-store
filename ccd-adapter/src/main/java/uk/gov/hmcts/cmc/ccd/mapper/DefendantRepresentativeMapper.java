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
    private final DefendantContactDetailsMapper defendantContactDetailsMapper;

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
            .claimantProvidedRepresentativeOrganisationName(representative.getOrganisationName())
            .claimantProvidedRepresentativeOrganisationAddress(
                addressMapper.to(representative.getOrganisationAddress())
            );
    }

    @Override
    public Representative from(CCDRespondent ccdRespondent) {
        if (isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationName())
            && ccdRespondent.getClaimantProvidedRepresentativeOrganisationAddress() == null
            && isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationDxAddress())
            && isBlank(ccdRespondent.getClaimantProvidedRepresentativeOrganisationEmail())
        ) {
            return null;
        }

        return Representative.builder()
            .organisationName(ccdRespondent.getClaimantProvidedRepresentativeOrganisationName())
            .organisationAddress(
                addressMapper.from(ccdRespondent.getClaimantProvidedRepresentativeOrganisationAddress())
            )
            .organisationContactDetails(defendantContactDetailsMapper.from(ccdRespondent))
            .build();

    }
}
