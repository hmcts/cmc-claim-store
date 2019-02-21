package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

@Component
public class IndividualDetailsMapper {

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

    public void to(IndividualDetails individual, CCDRespondent.CCDRespondentBuilder builder) {

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

    public IndividualDetails from(CCDCollectionElement<CCDRespondent> ccdRespondent) {
        CCDRespondent value = ccdRespondent.getValue();

        return IndividualDetails.builder()
            .id(ccdRespondent.getId())
            .name(value.getClaimantProvidedName())
            .address(addressMapper.from(value.getClaimantProvidedAddress()))
            .email(value.getClaimantProvidedEmail())
            .representative(representativeMapper.from(value))
            .serviceAddress(addressMapper.from(value.getClaimantProvidedServiceAddress()))
            .dateOfBirth(value.getClaimantProvidedDateOfBirth())
            .build();
    }
}
