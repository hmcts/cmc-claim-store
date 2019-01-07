package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;

@Component
public class OrganisationDetailsMapper
    implements BuilderMapper<CCDDefendant, OrganisationDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(OrganisationDetails organisation, CCDDefendant.CCDDefendantBuilder builder) {

        organisation.getServiceAddress()
            .ifPresent(address -> builder.claimantProvidedServiceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(builder::claimantProvidedContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::claimantProvidedCompaniesHouseNumber);
        organisation.getEmail().ifPresent(builder::claimantProvidedEmail);
        builder
            .claimantProvidedName(organisation.getName())
            .claimantProvidedAddress(addressMapper.to(organisation.getAddress()));
    }

    @Override
    public OrganisationDetails from(CCDDefendant ccdOrganisation) {

        return new OrganisationDetails(
            ccdOrganisation.getClaimantProvidedName(),
            addressMapper.from(ccdOrganisation.getClaimantProvidedAddress()),
            ccdOrganisation.getClaimantProvidedEmail(),
            representativeMapper.from(ccdOrganisation),
            addressMapper.from(ccdOrganisation.getClaimantProvidedServiceAddress()),
            ccdOrganisation.getClaimantProvidedContactPerson(),
            ccdOrganisation.getClaimantProvidedCompaniesHouseNumber()
        );
    }
}
