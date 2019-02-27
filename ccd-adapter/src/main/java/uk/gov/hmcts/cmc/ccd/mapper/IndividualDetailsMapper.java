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

        CCDParty.CCDPartyBuilder applicantProvidedDetails = CCDParty.builder().type(CCDPartyType.INDIVIDUAL);
        individual.getServiceAddress()
            .ifPresent(address -> applicantProvidedDetails.correspondenceAddress(addressMapper.to(address)));

        individual.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        individual.getDateOfBirth().ifPresent(applicantProvidedDetails::dateOfBirth);

        individual.getEmail().ifPresent(applicantProvidedDetails::emailAddress);
        applicantProvidedDetails.primaryAddress(addressMapper.to(individual.getAddress()));

        builder
            .applicantProvidedPartyName(individual.getName())
            .applicantProvidedDetail(applicantProvidedDetails.build());
    }

    public IndividualDetails from(CCDCollectionElement<CCDRespondent> ccdRespondent) {
        CCDRespondent respondent = ccdRespondent.getValue();
        CCDParty applicantProvidedPartyDetail = respondent.getApplicantProvidedDetail();

        return IndividualDetails.builder()
            .id(ccdRespondent.getId())
            .name(respondent.getApplicantProvidedPartyName())
            .address(addressMapper.from(applicantProvidedPartyDetail.getPrimaryAddress()))
            .email(applicantProvidedPartyDetail.getEmailAddress())
            .representative(representativeMapper.from(respondent))
            .serviceAddress(addressMapper.from(applicantProvidedPartyDetail.getCorrespondenceAddress()))
            .dateOfBirth(applicantProvidedPartyDetail.getDateOfBirth())
            .build();
    }
}
