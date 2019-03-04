package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;

@Component
public class OrganisationDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public OrganisationDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(OrganisationDetails organisation,
                   CCDRespondent.CCDRespondentBuilder builder,
                   CCDParty.CCDPartyBuilder claimantProvidedPartyDetail) {

        claimantProvidedPartyDetail.type(CCDPartyType.ORGANISATION);
        organisation.getServiceAddress()
            .ifPresent(address -> claimantProvidedPartyDetail.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(claimantProvidedPartyDetail::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(claimantProvidedPartyDetail::companiesHouseNumber);
        organisation.getEmail().ifPresent(claimantProvidedPartyDetail::emailAddress);
        claimantProvidedPartyDetail.primaryAddress(addressMapper.to(organisation.getAddress()));
        builder
            .claimantProvidedPartyName(organisation.getName())
            .partyDetail(claimantProvidedPartyDetail.build());
    }

    public OrganisationDetails from(CCDCollectionElement<CCDRespondent> ccdOrganisation) {
        CCDRespondent respondent = ccdOrganisation.getValue();
        CCDParty claimantProvidedDetails = respondent.getClaimantProvidedDetail();
        return OrganisationDetails.builder()
            .id(ccdOrganisation.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .address(addressMapper.from(claimantProvidedDetails.getPrimaryAddress()))
            .email(claimantProvidedDetails.getEmailAddress())
            .representative(representativeMapper.from(respondent))
            .serviceAddress(addressMapper.from(claimantProvidedDetails.getCorrespondenceAddress()))
            .contactPerson(claimantProvidedDetails.getContactPerson())
            .companiesHouseNumber(claimantProvidedDetails.getCompaniesHouseNumber())
            .build();
    }
}
