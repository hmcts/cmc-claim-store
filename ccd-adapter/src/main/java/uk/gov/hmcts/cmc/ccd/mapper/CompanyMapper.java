package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCompany;
import uk.gov.hmcts.cmc.domain.models.party.Company;

@Component
public class CompanyMapper implements Mapper<CCDCompany, Company> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public CompanyMapper(final AddressMapper addressMapper, final RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDCompany to(Company company) {

        final CCDCompany.CCDCompanyBuilder builder = CCDCompany.builder();
        company.getMobilePhone().ifPresent(builder::mobilePhone);
        company.getContactPerson().ifPresent(builder::contactPerson);

        company.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));

        builder
            .name(company.getName())
            .address(addressMapper.to(company.getAddress()));

        return builder.build();
    }

    @Override
    public Company from(CCDCompany company) {

        return new Company(
            company.getName(),
            addressMapper.from(company.getAddress()),
            addressMapper.from(company.getCorrespondenceAddress()),
            company.getMobilePhone(),
            representativeMapper.from(company.getRepresentative()),
            company.getContactPerson()
        );
    }
}
