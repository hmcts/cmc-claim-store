package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

@Component
public class SoleTraderDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

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

    public SoleTraderDetails from(CCDCollectionElement<CCDDefendant> ccdSoleTrader) {
        CCDDefendant value = ccdSoleTrader.getValue();

        return SoleTraderDetails.builder()
            .id(ccdSoleTrader.getId())
            .name(value.getClaimantProvidedName())
            .address(addressMapper.from(value.getClaimantProvidedAddress()))
            .email(value.getClaimantProvidedEmail())
            .representative(representativeMapper.from(value))
            .serviceAddress(addressMapper.from(value.getClaimantProvidedServiceAddress()))
            .title(value.getClaimantProvidedTitle())
            .businessName(value.getClaimantProvidedBusinessName())
            .build();
    }
}
