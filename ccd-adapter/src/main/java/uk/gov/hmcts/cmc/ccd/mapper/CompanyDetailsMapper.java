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

        company.getEmail().ifPresent(builder::claimantProvidedEmail);
        company.getContactPerson().ifPresent(builder::claimantProvidedContactPerson);

        company.getServiceAddress()
            .ifPresent(address -> builder.claimantProvidedServiceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        builder
            .claimantProvidedName(company.getName())
            .claimantProvidedAddress(addressMapper.to(company.getAddress()));

    }

    @Override
    public CompanyDetails from(CCDDefendant company) {

        return new CompanyDetails(
            company.getClaimantProvidedName(),
            addressMapper.from(company.getClaimantProvidedAddress()),
            company.getClaimantProvidedEmail(),
            representativeMapper.from(company),
            addressMapper.from(company.getClaimantProvidedServiceAddress()),
            company.getClaimantProvidedContactPerson()
        );
    }
}
