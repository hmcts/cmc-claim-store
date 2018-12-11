package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IndividualMapper implements Mapper<CCDIndividual, Individual> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public IndividualMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDIndividual to(Individual individual) {

        CCDIndividual.CCDIndividualBuilder builder = CCDIndividual.builder();
        individual.getMobilePhone().ifPresent(builder::phoneNumber);

        individual.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));

        if (individual.getDateOfBirth() != null) {
            builder.dateOfBirth(individual.getDateOfBirth().format(DateTimeFormatter.ISO_DATE));
        }

        return builder
            .name(individual.getName())
            .address(addressMapper.to(individual.getAddress()))
            .build();
    }

    @Override
    public Individual from(CCDIndividual individual) {

        return new Individual(
            individual.getName(),
            addressMapper.from(individual.getAddress()),
            addressMapper.from(individual.getCorrespondenceAddress()),
            individual.getPhoneNumber(),
            representativeMapper.from(individual.getRepresentative()),
            LocalDate.parse(individual.getDateOfBirth(), DateTimeFormatter.ISO_DATE)
        );
    }
}
