package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;

import java.util.Optional;
import java.util.function.Function;

@Component
public class OrganisationDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public OrganisationDetailsMapper(AddressMapper addressMapper,
                                     DefendantRepresentativeMapper representativeMapper,
                                     TelephoneMapper telephoneMapper
    ) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(OrganisationDetails organisation,
                   CCDRespondent.CCDRespondentBuilder builder) {

        CCDParty.CCDPartyBuilder claimantProvidedPartyDetail = CCDParty.builder().type(CCDPartyType.ORGANISATION);
        organisation.getServiceAddress()
            .ifPresent(address -> claimantProvidedPartyDetail.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(claimantProvidedPartyDetail::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(claimantProvidedPartyDetail::companiesHouseNumber);
        organisation.getEmail().ifPresent(claimantProvidedPartyDetail::emailAddress);
        organisation.getPhone()
            .ifPresent(phoneNo -> claimantProvidedPartyDetail.telephoneNumber(telephoneMapper.to(phoneNo)));
        if (organisation.getclaimantProvidedAddress() != null) {
            claimantProvidedPartyDetail.primaryAddress(addressMapper.to(organisation.getclaimantProvidedAddress()));
        } else {
            claimantProvidedPartyDetail.primaryAddress(addressMapper.to(organisation.getAddress()));
        }
        builder
            .claimantProvidedPartyName(organisation.getName())
            .claimantProvidedDetail(claimantProvidedPartyDetail.build());
    }

    public OrganisationDetails from(CCDCollectionElement<CCDRespondent> ccdOrganisation) {
        CCDRespondent respondent = ccdOrganisation.getValue();
        CCDParty detailFromClaimant = respondent.getClaimantProvidedDetail();
        CCDParty partyDetail = respondent.getPartyDetail();

        return OrganisationDetails.builder()
            .id(ccdOrganisation.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .address(getAddress(partyDetail, detailFromClaimant, CCDParty::getPrimaryAddress))
            .claimantProvidedAddress(getAddressByClaimant(detailFromClaimant, CCDParty::getPrimaryAddress))
            .email(getDetail(partyDetail, detailFromClaimant, x -> x.getEmailAddress()))
            .phoneNumber(telephoneMapper.from(getDetail(partyDetail, detailFromClaimant, CCDParty::getTelephoneNumber)))
            .representative(representativeMapper.from(respondent))
            .serviceAddress(getAddress(partyDetail, detailFromClaimant, CCDParty::getCorrespondenceAddress))
            .contactPerson(detailFromClaimant.getContactPerson())
            .companiesHouseNumber(detailFromClaimant.getCompaniesHouseNumber())
            .build();
    }

    private Address getAddress(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, CCDAddress> getAddress) {
        CCDAddress partyAddress = getAddress(detail, getAddress);
        return addressMapper.from(partyAddress != null ? partyAddress : getAddress(detailByClaimant, getAddress));
    }

    private CCDAddress getAddress(CCDParty partyDetail, Function<CCDParty, CCDAddress> extractAddress) {
        return Optional.ofNullable(partyDetail).map(extractAddress).orElse(null);
    }

    private Address getAddressByClaimant(CCDParty detail, Function<CCDParty, CCDAddress> getAddress) {
        CCDAddress partyAddress = getAddress(detail, getAddress);
        return addressMapper.from(partyAddress);
    }

    private <T> T getDetail(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, T> getDetail) {
        T partyDetail = detail != null ? getDetail.apply(detail) : null;
        return partyDetail != null ? partyDetail : getDetail.apply(detailByClaimant);
    }
}
