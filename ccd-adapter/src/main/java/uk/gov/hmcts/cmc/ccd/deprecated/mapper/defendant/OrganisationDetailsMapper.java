package uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.RepresentativeMapper;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;

//@Component
public class OrganisationDetailsMapper implements Mapper<CCDOrganisation, OrganisationDetails> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationDetailsMapper(AddressMapper addressMapper,
                                     RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDOrganisation to(OrganisationDetails organisation) {

        CCDOrganisation.CCDOrganisationBuilder builder = CCDOrganisation.builder();
        organisation.getServiceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));
        organisation.getEmail().ifPresent(builder::email);
        organisation.getContactPerson().ifPresent(builder::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::companiesHouseNumber);
        builder
            .name(organisation.getName())
            .address(addressMapper.to(organisation.getAddress()));

        return builder.build();
    }

    @Override
    public OrganisationDetails from(CCDOrganisation ccdOrganisation) {

        return new OrganisationDetails(
            ccdOrganisation.getName(),
            addressMapper.from(ccdOrganisation.getAddress()),
            ccdOrganisation.getEmail(),
            representativeMapper.from(ccdOrganisation.getRepresentative()),
            addressMapper.from(ccdOrganisation.getCorrespondenceAddress()),
            ccdOrganisation.getContactPerson(),
            ccdOrganisation.getCompaniesHouseNumber()
        );
    }
}
