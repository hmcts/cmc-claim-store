package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.party.Company;

@Component
public class CompanyMapper implements BuilderMapper<CCDClaimant, Company, CCDClaimant.CCDClaimantBuilder> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public CompanyMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(Company company, CCDClaimant.CCDClaimantBuilder builder) {

        company.getMobilePhone().ifPresent(builder::partyPhoneNumber);
        company.getContactPerson().ifPresent(builder::partyContactPerson);

        company.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        builder
            .partyName(company.getName())
            .partyAddress(addressMapper.to(company.getAddress()));

    }

    @Override
    public Company from(CCDClaimant company) {

        return new Company(
            company.getPartyName(),
            addressMapper.from(company.getPartyAddress()),
            addressMapper.from(company.getPartyCorrespondenceAddress()),
            company.getPartyPhoneNumber(),
            representativeMapper.from(company),
            company.getPartyContactPerson()
        );
    }
}
