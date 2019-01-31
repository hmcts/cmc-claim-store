package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.util.Optional;

@Component
public class IndividualMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public IndividualMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(Individual individual, CCDClaimant.CCDClaimantBuilder builder) {

        individual.getMobilePhone().ifPresent(builder::partyPhone);

        individual.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        Optional.ofNullable(individual.getDateOfBirth()).ifPresent(builder::partyDateOfBirth);

        builder
            .partyName(individual.getName())
            .partyAddress(addressMapper.to(individual.getAddress()));
    }

    public Individual from(CCDCollectionElement<CCDClaimant> individual) {
        CCDClaimant value = individual.getValue();
        return Individual.builder()
            .id(individual.getId())
            .name(value.getPartyName())
            .address(addressMapper.from(value.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(value.getPartyCorrespondenceAddress()))
            .mobilePhone(value.getPartyPhone())
            .representative(representativeMapper.from(value))
            .dateOfBirth(value.getPartyDateOfBirth())
            .build();
    }
}
