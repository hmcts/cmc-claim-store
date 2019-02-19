package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
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

    public void to(OrganisationDetails organisation, CCDDefendant.CCDDefendantBuilder builder) {

        organisation.getServiceAddress()
            .ifPresent(address -> builder.claimantProvidedServiceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getContactPerson().ifPresent(builder::claimantProvidedContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::claimantProvidedCompaniesHouseNumber);
        organisation.getEmail().ifPresent(builder::claimantProvidedEmail);
        builder
            .claimantProvidedName(organisation.getName())
            .claimantProvidedAddress(addressMapper.to(organisation.getAddress()));
    }

    public OrganisationDetails from(CCDCollectionElement<CCDDefendant> ccdOrganisation) {
        CCDDefendant value = ccdOrganisation.getValue();

        return OrganisationDetails.builder()
            .id(ccdOrganisation.getId())
            .name(value.getClaimantProvidedName())
            .address(addressMapper.from(value.getClaimantProvidedAddress()))
            .email(value.getClaimantProvidedEmail())
            .representative(representativeMapper.from(value))
            .serviceAddress(addressMapper.from(value.getClaimantProvidedServiceAddress()))
            .contactPerson(value.getClaimantProvidedContactPerson())
            .companiesHouseNumber(value.getClaimantProvidedCompaniesHouseNumber())
            .build();
    }
}
