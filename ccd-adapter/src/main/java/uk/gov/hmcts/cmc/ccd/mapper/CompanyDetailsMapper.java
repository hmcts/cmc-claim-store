package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

@Component
public class CompanyDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public CompanyDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(CompanyDetails company, CCDRespondent.CCDRespondentBuilder builder) {

        company.getEmail().ifPresent(builder::claimantProvidedEmail);
        company.getContactPerson().ifPresent(builder::claimantProvidedContactPerson);

        company.getServiceAddress()
            .ifPresent(address -> builder.claimantProvidedServiceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        builder
            .claimantProvidedName(company.getName())
            .claimantProvidedAddress(addressMapper.to(company.getAddress()));

    }

    public CompanyDetails from(CCDCollectionElement<CCDRespondent> collectionElement) {
        CCDRespondent ccdRespondent = collectionElement.getValue();

        return CompanyDetails.builder()
            .id(collectionElement.getId())
            .name(ccdRespondent.getClaimantProvidedName())
            .address(addressMapper.from(ccdRespondent.getClaimantProvidedAddress()))
            .email(ccdRespondent.getClaimantProvidedEmail())
            .representative(representativeMapper.from(ccdRespondent))
            .serviceAddress(addressMapper.from(ccdRespondent.getClaimantProvidedServiceAddress()))
            .contactPerson(ccdRespondent.getClaimantProvidedContactPerson())
            .build();
    }
}
