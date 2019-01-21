package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.party.Company;

@Component
public class CompanyMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public CompanyMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(Company company, CCDClaimant.CCDClaimantBuilder builder) {

        company.getMobilePhone().ifPresent(builder::partyPhone);
        company.getContactPerson().ifPresent(builder::partyContactPerson);

        company.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        builder
            .partyName(company.getName())
            .partyAddress(addressMapper.to(company.getAddress()));

    }

    public Company from(CCDCollectionElement<CCDClaimant> company) {
        CCDClaimant value = company.getValue();
        return Company.builder()
            .id(company.getId())
            .name(value.getPartyName())
            .address(addressMapper.from(value.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(value.getPartyCorrespondenceAddress()))
            .mobilePhone(value.getPartyPhone())
            .representative(representativeMapper.from(value))
            .contactPerson(value.getPartyContactPerson())
            .build();
    }
}
