package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IndividualDetailsMapper implements BuilderMapper<CCDDefendant, IndividualDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private DefendantRepresentativeMapper defendantRepresentativeMapper;

    @Autowired
    public IndividualDetailsMapper(
        AddressMapper addressMapper,
        DefendantRepresentativeMapper defendantRepresentativeMapper
    ) {
        this.addressMapper = addressMapper;
        this.defendantRepresentativeMapper = defendantRepresentativeMapper;
    }

    @Override
    public void to(IndividualDetails individual, CCDDefendant.CCDDefendantBuilder builder) {

        individual.getServiceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> defendantRepresentativeMapper.to(representative, builder));

        individual.getDateOfBirth().ifPresent(dob ->
            builder.partyDateOfBirth(dob.format(DateTimeFormatter.ISO_DATE)));


        individual.getEmail().ifPresent(builder::partyEmail);

        builder
            .partyName(individual.getName())
            .partyAddress(addressMapper.to(individual.getAddress()));
    }

    @Override
    public IndividualDetails from(CCDDefendant ccdDefendant) {

        return new IndividualDetails(
            ccdDefendant.getPartyName(),
            addressMapper.from(ccdDefendant.getPartyAddress()),
            ccdDefendant.getPartyEmail(),
            defendantRepresentativeMapper.from(ccdDefendant),
            addressMapper.from(ccdDefendant.getPartyServiceAddress()),
            LocalDate.parse(ccdDefendant.getPartyDateOfBirth(), DateTimeFormatter.ISO_DATE)
        );
    }
}
