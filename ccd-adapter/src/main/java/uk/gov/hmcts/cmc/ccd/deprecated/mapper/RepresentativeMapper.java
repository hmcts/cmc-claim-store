package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

//@Component
public class RepresentativeMapper implements Mapper<CCDRepresentative, Representative> {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;

    @Autowired
    public RepresentativeMapper(AddressMapper addressMapper, ContactDetailsMapper contactDetailsMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
    }

    @Override
    public CCDRepresentative to(Representative representative) {

        CCDRepresentative.CCDRepresentativeBuilder builder = CCDRepresentative.builder();
        representative.getOrganisationContactDetails()
            .ifPresent(organisationContactDetails -> builder.organisationContactDetails(
                contactDetailsMapper.to(organisationContactDetails))
            );

        return builder
            .organisationName(representative.getOrganisationName())
            .organisationAddress(addressMapper.to(representative.getOrganisationAddress()))
            .build();
    }

    @Override
    public Representative from(CCDRepresentative representative) {
        if (representative == null) {
            return null;
        }

        return new Representative(
            representative.getOrganisationName(),
            addressMapper.from(representative.getOrganisationAddress()),
            contactDetailsMapper.from(representative.getOrganisationContactDetails().orElse(null))
        );
    }
}
