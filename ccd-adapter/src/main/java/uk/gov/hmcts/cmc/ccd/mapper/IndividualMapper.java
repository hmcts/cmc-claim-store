package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.util.Optional;

@Component
public class IndividualMapper implements BuilderMapper<CCDClaimant, Individual, CCDClaimant.CCDClaimantBuilder> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public IndividualMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
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

    @Override
    public Individual from(CCDClaimant ccdClaimant) {

        return new Individual(
            ccdClaimant.getPartyName(),
            addressMapper.from(ccdClaimant.getPartyAddress()),
            addressMapper.from(ccdClaimant.getPartyCorrespondenceAddress()),
            ccdClaimant.getPartyPhone(),
            representativeMapper.from(ccdClaimant),
            ccdClaimant.getPartyDateOfBirth()
        );
    }
}
