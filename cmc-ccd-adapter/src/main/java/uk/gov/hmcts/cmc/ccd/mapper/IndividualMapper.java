package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDIndividual;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IndividualMapper implements Mapper<CCDIndividual, Individual> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public IndividualMapper(final AddressMapper addressMapper, final RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDIndividual to(Individual individual) {

        final CCDIndividual.CCDIndividualBuilder builder = CCDIndividual.builder();
        individual.getTitle().ifPresent(builder::title);
        individual.getMobilePhone().ifPresent(builder::mobilePhone);

        individual.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));

        return builder
            .name(individual.getName())
            .dateOfBirth(individual.getDateOfBirth().format(DateTimeFormatter.ISO_DATE))
            .address(addressMapper.to(individual.getAddress()))
            .build();
    }

    @Override
    public Individual from(CCDIndividual individual) {

        return new Individual(
            individual.getName(),
            addressMapper.from(individual.getAddress()),
            addressMapper.from(individual.getCorrespondenceAddress()),
            individual.getMobilePhone(),
            representativeMapper.from(individual.getRepresentative()),
            individual.getTitle(),
            LocalDate.parse(individual.getDateOfBirth(), DateTimeFormatter.ISO_DATE)
        );
    }
}
