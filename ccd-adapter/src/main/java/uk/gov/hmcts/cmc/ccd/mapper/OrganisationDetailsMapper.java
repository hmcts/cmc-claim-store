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

    public void to(OrganisationDetails organisation, CCDRespondent.CCDRespondentBuilder builder) {
        CCDParty.CCDPartyBuilder applicantProvidedPartyDetail = CCDParty.builder().type(CCDPartyType.ORGANISATION);
        organisation.getServiceAddress()
            .ifPresent(address -> applicantProvidedPartyDetail.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(applicantProvidedPartyDetail::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(applicantProvidedPartyDetail::companiesHouseNumber);
        organisation.getEmail().ifPresent(applicantProvidedPartyDetail::emailAddress);
        applicantProvidedPartyDetail.primaryAddress(addressMapper.to(organisation.getAddress()));
        builder
            .applicantProvidedPartyName(organisation.getName())
            .partyDetail(applicantProvidedPartyDetail.build());
    }

    public OrganisationDetails from(CCDCollectionElement<CCDRespondent> ccdOrganisation) {
        CCDRespondent respondent = ccdOrganisation.getValue();
        CCDParty applicantProvidedDetails = respondent.getApplicantProvidedDetail();
        return OrganisationDetails.builder()
            .id(ccdOrganisation.getId())
            .name(respondent.getApplicantProvidedPartyName())
            .address(addressMapper.from(applicantProvidedDetails.getPrimaryAddress()))
            .email(applicantProvidedDetails.getEmailAddress())
            .representative(representativeMapper.from(respondent))
            .serviceAddress(addressMapper.from(applicantProvidedDetails.getCorrespondenceAddress()))
            .contactPerson(applicantProvidedDetails.getContactPerson())
            .companiesHouseNumber(applicantProvidedDetails.getCompaniesHouseNumber())
            .build();
    }
}
