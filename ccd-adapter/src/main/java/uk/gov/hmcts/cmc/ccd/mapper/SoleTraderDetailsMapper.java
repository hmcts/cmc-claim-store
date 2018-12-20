package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

@Component
public class SoleTraderDetailsMapper
    implements BuilderMapper<CCDDefendant, SoleTraderDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(SoleTraderDetails soleTrader, CCDDefendant.CCDDefendantBuilder builder) {

        soleTrader.getTitle().ifPresent(builder::claimantProvidedTitle);
        soleTrader.getBusinessName().ifPresent(builder::claimantProvidedBusinessName);
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        soleTrader.getEmail().ifPresent(builder::claimantProvidedEmail);
        soleTrader.getServiceAddress().ifPresent(addressMapper::to);

        builder
            .claimantProvidedName(soleTrader.getName())
            .claimantProvidedAddress(addressMapper.to(soleTrader.getAddress()));

    }

    @Override
    public SoleTraderDetails from(CCDDefendant ccdSoleTrader) {
        return new SoleTraderDetails(
            ccdSoleTrader.getClaimantProvidedName(),
            addressMapper.from(ccdSoleTrader.getClaimantProvidedAddress()),
            ccdSoleTrader.getClaimantProvidedEmail(),
            representativeMapper.from(ccdSoleTrader),
            addressMapper.from(ccdSoleTrader.getClaimantProvidedServiceAddress()),
            ccdSoleTrader.getClaimantProvidedTitle(),
            ccdSoleTrader.getClaimantProvidedBusinessName()
        );
    }
}
