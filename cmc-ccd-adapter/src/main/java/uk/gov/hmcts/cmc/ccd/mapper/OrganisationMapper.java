package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationMapper implements Mapper<CCDOrganisation, Organisation> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationMapper(final AddressMapper addressMapper, final RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDOrganisation to(Organisation organisation) {

        return CCDOrganisation.builder()
            .name(organisation.getName())
            .address(addressMapper.to(organisation.getAddress()))
            .correspondenceAddress(addressMapper.to(organisation.getCorrespondenceAddress().orElse(null)))
            .mobilePhone(organisation.getMobilePhone().orElse(null))
            .representative(representativeMapper.to(organisation.getRepresentative().orElse(null)))
            .companiesHouseNumber(organisation.getCompaniesHouseNumber().orElse(null))
            .build();
    }

    @Override
    public Organisation from(CCDOrganisation ccdOrganisation) {

        return new Organisation(
            ccdOrganisation.getName(),
            addressMapper.from(ccdOrganisation.getAddress()),
            addressMapper.from(ccdOrganisation.getCorrespondenceAddress()),
            ccdOrganisation.getMobilePhone(),
            representativeMapper.from(ccdOrganisation.getRepresentative()),
            ccdOrganisation.getContactPerson(),
            ccdOrganisation.getCompaniesHouseNumber()
        );
    }
}
