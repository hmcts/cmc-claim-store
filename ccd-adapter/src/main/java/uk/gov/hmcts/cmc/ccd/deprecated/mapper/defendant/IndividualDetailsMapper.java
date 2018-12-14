package uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.RepresentativeMapper;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

//@Component
public class IndividualDetailsMapper implements Mapper<CCDIndividual, IndividualDetails> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public IndividualDetailsMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDIndividual to(IndividualDetails individual) {

        CCDIndividual.CCDIndividualBuilder builder = CCDIndividual.builder();
        individual.getEmail().ifPresent(builder::email);

        individual.getServiceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));
        individual.getDateOfBirth().ifPresent(dob -> builder.dateOfBirth(dob.format(ISO_DATE)));

        return builder
            .name(individual.getName())
            .address(addressMapper.to(individual.getAddress()))
            .build();
    }

    @Override
    public IndividualDetails from(CCDIndividual individual) {

        return new IndividualDetails(
            individual.getName(),
            addressMapper.from(individual.getAddress()),
            individual.getEmail(),
            representativeMapper.from(individual.getRepresentative()),
            addressMapper.from(individual.getCorrespondenceAddress()),
            LocalDate.parse(individual.getDateOfBirth(), ISO_DATE)
        );
    }
}
