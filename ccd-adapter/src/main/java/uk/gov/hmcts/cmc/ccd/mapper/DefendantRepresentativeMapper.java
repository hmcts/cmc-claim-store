package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DefendantRepresentativeMapper
    implements BuilderMapper<CCDDefendant, Representative, CCDDefendant.CCDDefendantBuilder> {

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
    public void to(Representative representative, CCDDefendant.CCDDefendantBuilder builder) {

        representative.getOrganisationContactDetails().ifPresent(organisationContactDetails ->
            defendantContactDetailsMapper.to(organisationContactDetails, builder));

        builder
            .claimantProvidedRepresentativeOrganisationName(representative.getOrganisationName())
            .claimantProvidedRepresentativeOrganisationAddress(
                addressMapper.to(representative.getOrganisationAddress())
            );
    }

    @Override
    public Representative from(CCDDefendant ccdDefendant) {
        if (isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationName())
            && ccdDefendant.getClaimantProvidedRepresentativeOrganisationAddress() == null
            && isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationPhone())
            && isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationDxAddress())
            && isBlank(ccdDefendant.getClaimantProvidedRepresentativeOrganisationEmail())
        ) {
            return null;
        }

        return new Representative(
            ccdDefendant.getClaimantProvidedRepresentativeOrganisationName(),
            addressMapper.from(ccdDefendant.getClaimantProvidedRepresentativeOrganisationAddress()),
            defendantContactDetailsMapper.from(ccdDefendant)
        );

    }
}
