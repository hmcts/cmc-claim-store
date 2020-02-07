package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

@Component
public class IndividualDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public IndividualDetailsMapper(
        AddressMapper addressMapper,
        DefendantRepresentativeMapper defendantRepresentativeMapper,
        TelephoneMapper telephoneMapper
    ) {
        this.addressMapper = addressMapper;
        this.representativeMapper = defendantRepresentativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(IndividualDetails individual,
                   CCDRespondent.CCDRespondentBuilder builder) {

        CCDParty.CCDPartyBuilder claimantProvidedDetails = CCDParty.builder().type(CCDPartyType.INDIVIDUAL);
        claimantProvidedDetails.firstName(individual.getFirstName());
        claimantProvidedDetails.lastName(individual.getLastName());
        individual.getTitle().ifPresent(claimantProvidedDetails::title);
        individual.getServiceAddress()
            .ifPresent(address -> claimantProvidedDetails.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        individual.getDateOfBirth().ifPresent(claimantProvidedDetails::dateOfBirth);
        individual.getPhone()
            .ifPresent(phoneNo -> claimantProvidedDetails.telephoneNumber(telephoneMapper.to(phoneNo)));

        individual.getEmail().ifPresent(claimantProvidedDetails::emailAddress);
        claimantProvidedDetails.primaryAddress(addressMapper.to(individual.getAddress()));

        builder
            .claimantProvidedPartyName(individual.getName())
            .claimantProvidedDetail(claimantProvidedDetails.build());
    }

    public IndividualDetails from(CCDCollectionElement<CCDRespondent> ccdRespondent) {
        CCDRespondent respondent = ccdRespondent.getValue();
        CCDParty claimantProvidedPartyDetail = respondent.getClaimantProvidedDetail();

        return IndividualDetails.builder()
            .id(ccdRespondent.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .firstName(respondent.getClaimantProvidedDetail().getFirstName())
            .lastName(respondent.getClaimantProvidedDetail().getLastName())
            .title(respondent.getClaimantProvidedDetail().getTitle())
            .address(addressMapper.from(claimantProvidedPartyDetail.getPrimaryAddress()))
            .email(claimantProvidedPartyDetail.getEmailAddress())
            .phoneNumber(telephoneMapper.from(claimantProvidedPartyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(respondent))
            .serviceAddress(addressMapper.from(claimantProvidedPartyDetail.getCorrespondenceAddress()))
            .dateOfBirth(claimantProvidedPartyDetail.getDateOfBirth())
            .build();
    }
}
