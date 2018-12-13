package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

@Component
public class CompanyDetailsMapper
    implements BuilderMapper<CCDDefendant, CompanyDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public CompanyDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(CompanyDetails company, CCDDefendant.CCDDefendantBuilder builder) {

        company.getEmail().ifPresent(builder::partyEmail);
        company.getContactPerson().ifPresent(builder::partyContactPerson);

        company.getServiceAddress()
            .ifPresent(address -> builder.partyServiceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        builder
            .partyName(company.getName())
            .partyAddress(addressMapper.to(company.getAddress()));

    }

    @Override
    public CompanyDetails from(CCDDefendant company) {

        return new CompanyDetails(
            company.getPartyName(),
            addressMapper.from(company.getPartyAddress()),
            company.getPartyEmail(),
            representativeMapper.from(company),
            addressMapper.from(company.getPartyServiceAddress()),
            company.getPartyContactPerson()
        );
    }
}
