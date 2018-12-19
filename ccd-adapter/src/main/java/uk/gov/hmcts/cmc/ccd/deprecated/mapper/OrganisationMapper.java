package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationMapper implements Mapper<CCDOrganisation, Organisation> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDOrganisation to(Organisation organisation) {

        CCDOrganisation.CCDOrganisationBuilder builder = CCDOrganisation.builder();
        organisation.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));
        organisation.getMobilePhone().ifPresent(builder::phoneNumber);
        organisation.getContactPerson().ifPresent(builder::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::companiesHouseNumber);
        builder
            .name(organisation.getName())
            .address(addressMapper.to(organisation.getAddress()));

        return builder.build();
    }

    @Override
    public Organisation from(CCDOrganisation ccdOrganisation) {

        return new Organisation(
            ccdOrganisation.getName(),
            addressMapper.from(ccdOrganisation.getAddress()),
            addressMapper.from(ccdOrganisation.getCorrespondenceAddress()),
            ccdOrganisation.getPhoneNumber(),
            representativeMapper.from(ccdOrganisation.getRepresentative()),
            ccdOrganisation.getContactPerson(),
            ccdOrganisation.getCompaniesHouseNumber()
        );
    }
}
