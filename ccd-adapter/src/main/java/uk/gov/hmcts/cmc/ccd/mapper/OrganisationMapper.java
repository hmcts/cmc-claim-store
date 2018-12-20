package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationMapper implements BuilderMapper<CCDClaimant, Organisation, CCDClaimant.CCDClaimantBuilder> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(Organisation organisation, CCDClaimant.CCDClaimantBuilder builder) {

        organisation.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getMobilePhone().ifPresent(builder::partyPhoneNumber);
        organisation.getContactPerson().ifPresent(builder::partyContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::partyCompaniesHouseNumber);
        builder
            .partyName(organisation.getName())
            .partyAddress(addressMapper.to(organisation.getAddress()));

    }

    @Override
    public Organisation from(CCDClaimant ccdOrganisation) {
        return new Organisation(
            ccdOrganisation.getPartyName(),
            addressMapper.from(ccdOrganisation.getPartyAddress()),
            addressMapper.from(ccdOrganisation.getPartyCorrespondenceAddress()),
            ccdOrganisation.getPartyPhoneNumber(),
            representativeMapper.from(ccdOrganisation),
            ccdOrganisation.getPartyContactPerson(),
            ccdOrganisation.getPartyCompaniesHouseNumber()
        );
    }
}
