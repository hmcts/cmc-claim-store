package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationDetailsMapper implements BuilderMapper<CCDDefendant, OrganisationDetails, CCDDefendant.CCDDefendantBuilder> {

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
            .ifPresent(address -> builder.partyServiceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(builder::partyContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::partyCompaniesHouseNumber);
        builder
            .partyName(organisation.getName())
            .partyAddress(addressMapper.to(organisation.getAddress()));

    }

    @Override
    public OrganisationDetails from(CCDDefendant ccdOrganisation) {

        return new OrganisationDetails(
            ccdOrganisation.getPartyName(),
            addressMapper.from(ccdOrganisation.getPartyAddress()),
            ccdOrganisation.getPartyEmail(),
            representativeMapper.from(ccdOrganisation),
            addressMapper.from(ccdOrganisation.getPartyServiceAddress()),
            ccdOrganisation.getPartyContactPerson(),
            ccdOrganisation.getPartyCompaniesHouseNumber()
        );
    }
}
