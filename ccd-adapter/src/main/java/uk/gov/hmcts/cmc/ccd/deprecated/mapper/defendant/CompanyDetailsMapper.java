package uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.RepresentativeMapper;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

//@Component
public class CompanyDetailsMapper implements Mapper<CCDCompany, CompanyDetails> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public CompanyDetailsMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDCompany to(CompanyDetails company) {

        CCDCompany.CCDCompanyBuilder builder = CCDCompany.builder();
        company.getContactPerson().ifPresent(builder::contactPerson);
        company.getEmail().ifPresent(builder::email);

        company.getServiceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));

        builder
            .name(company.getName())
            .address(addressMapper.to(company.getAddress()));

        return builder.build();
    }

    @Override
    public CompanyDetails from(CCDCompany company) {

        return new CompanyDetails(
            company.getName(),
            addressMapper.from(company.getAddress()),
            company.getEmail(),
            representativeMapper.from(company.getRepresentative()),
            addressMapper.from(company.getCorrespondenceAddress()),
            company.getContactPerson()
        );
    }
}
