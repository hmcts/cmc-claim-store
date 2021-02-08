package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

import java.util.Optional;
import java.util.function.Function;

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
        CCDParty partyDetail = respondent.getPartyDetail();
        CCDParty detailFromClaimant = respondent.getClaimantProvidedDetail();

        return IndividualDetails.builder()
            .id(ccdRespondent.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .firstName(respondent.getClaimantProvidedDetail().getFirstName())
            .lastName(respondent.getClaimantProvidedDetail().getLastName())
            .title(respondent.getClaimantProvidedDetail().getTitle())
            .address(getAddress1(detailFromClaimant, CCDParty::getPrimaryAddress))
            .claimantProvidedAddress(getAddress1(detailFromClaimant, CCDParty::getPrimaryAddress))
            .email(getDetail(partyDetail, detailFromClaimant, x -> x.getEmailAddress()))
            .phoneNumber(telephoneMapper.from(getDetail(partyDetail, detailFromClaimant, CCDParty::getTelephoneNumber)))
            .representative(representativeMapper.from(respondent))
            .serviceAddress(getAddress(partyDetail, detailFromClaimant, CCDParty::getCorrespondenceAddress))
            .dateOfBirth(detailFromClaimant.getDateOfBirth())
            .build();
    }

    private Address getAddress(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, CCDAddress> getAddress) {
        CCDAddress partyAddress = getAddress(detail, getAddress);
        return addressMapper.from((partyAddress != null && detailByClaimant == null)
            ? partyAddress : getAddress(detailByClaimant, getAddress));
    }
    //partyaddress !=null && paryaddress.notequals claimaint address --> partyAddress

    private Address getAddress1(CCDParty detail, Function<CCDParty, CCDAddress> getAddress) {
        CCDAddress partyAddress = getAddress(detail, getAddress);
        return addressMapper.from(partyAddress);
    }

    private CCDAddress getAddress(CCDParty partyDetail, Function<CCDParty, CCDAddress> extractAddress) {
        return Optional.ofNullable(partyDetail).map(extractAddress).orElse(null);
    }

    private <T> T getDetail(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, T> getDetail) {
        T partyDetail = detail != null ? getDetail.apply(detail) : null;
        return partyDetail != null ? partyDetail : getDetail.apply(detailByClaimant);
    }

}
