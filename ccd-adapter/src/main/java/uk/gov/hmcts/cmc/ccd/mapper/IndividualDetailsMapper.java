package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

@Component
public class IndividualDetailsMapper
    implements BuilderMapper<CCDDefendant, IndividualDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public IndividualDetailsMapper(
        AddressMapper addressMapper,
        DefendantRepresentativeMapper defendantRepresentativeMapper
    ) {
        this.addressMapper = addressMapper;
        this.representativeMapper = defendantRepresentativeMapper;
    }

    @Override
    public void to(IndividualDetails individual, CCDDefendant.CCDDefendantBuilder builder) {

        individual.getServiceAddress()
            .ifPresent(address -> builder.claimantProvidedServiceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        individual.getDateOfBirth().ifPresent(builder::claimantProvidedDateOfBirth);

        individual.getEmail().ifPresent(builder::claimantProvidedEmail);

        builder
            .claimantProvidedName(individual.getName())
            .claimantProvidedAddress(addressMapper.to(individual.getAddress()));
    }

    @Override
    public IndividualDetails from(CCDDefendant ccdDefendant) {

        return new IndividualDetails(
            ccdDefendant.getClaimantProvidedName(),
            addressMapper.from(ccdDefendant.getClaimantProvidedAddress()),
            ccdDefendant.getClaimantProvidedEmail(),
            representativeMapper.from(ccdDefendant),
            addressMapper.from(ccdDefendant.getClaimantProvidedServiceAddress()),
            ccdDefendant.getClaimantProvidedDateOfBirth()
        );
    }
}
