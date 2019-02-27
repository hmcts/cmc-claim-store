package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.util.Optional;

@Component
public class IndividualMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public IndividualMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper,
                            TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(Individual individual, CCDApplicant.CCDApplicantBuilder builder) {
        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder().type(CCDPartyType.INDIVIDUAL);
        individual.getMobilePhone()
            .ifPresent(telephoneNo -> partyDetail.telephoneNumber(telephoneMapper.to(telephoneNo)));

        individual.getCorrespondenceAddress()
            .ifPresent(address -> partyDetail.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        Optional.ofNullable(individual.getDateOfBirth()).ifPresent(partyDetail::dateOfBirth);
        partyDetail.primaryAddress(addressMapper.to(individual.getAddress()));

        builder
            .partyName(individual.getName())
            .partyDetail(partyDetail.build());
    }

    public Individual from(CCDCollectionElement<CCDApplicant> individual) {
        CCDApplicant applicant = individual.getValue();
        CCDParty partyDetails = applicant.getPartyDetail();
        return Individual.builder()
            .id(individual.getId())
            .name(applicant.getPartyName())
            .address(addressMapper.from(partyDetails.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetails.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetails.getTelephoneNumber()))
            .representative(representativeMapper.from(applicant))
            .dateOfBirth(partyDetails.getDateOfBirth())
            .build();
    }
}
