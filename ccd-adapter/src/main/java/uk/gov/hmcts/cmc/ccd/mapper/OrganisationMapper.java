package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(Organisation organisation, CCDApplicant.CCDApplicantBuilder builder) {

        organisation.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getMobilePhone().ifPresent(builder::partyPhone);
        organisation.getContactPerson().ifPresent(builder::partyContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::partyCompaniesHouseNumber);
        builder
            .partyName(organisation.getName())
            .partyAddress(addressMapper.to(organisation.getAddress()));

    }

    public Organisation from(CCDCollectionElement<CCDApplicant> organisation) {
        CCDApplicant value = organisation.getValue();
        return Organisation.builder()
            .id(organisation.getId())
            .name(value.getPartyName())
            .address(addressMapper.from(value.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(value.getPartyCorrespondenceAddress()))
            .mobilePhone(value.getPartyPhone())
            .representative(representativeMapper.from(value))
            .contactPerson(value.getPartyContactPerson())
            .companiesHouseNumber(value.getPartyCompaniesHouseNumber())
            .build();
    }
}
