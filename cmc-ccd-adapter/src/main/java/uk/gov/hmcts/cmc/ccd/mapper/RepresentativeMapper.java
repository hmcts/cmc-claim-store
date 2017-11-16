package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.Representative;

@Component
public class RepresentativeMapper
    implements Mapper<Representative, uk.gov.hmcts.cmc.domain.models.legalrep.Representative> {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;

    @Autowired
    public RepresentativeMapper(final AddressMapper addressMapper, final ContactDetailsMapper contactDetailsMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
    }

    @Override
    public Representative to(uk.gov.hmcts.cmc.domain.models.legalrep.Representative representative) {
        if (representative == null) {
            return null;
        }

        return Representative.builder()
            .organisationName(representative.getOrganisationName())
            .organisationAddress(addressMapper.to(representative.getOrganisationAddress()))
            .organisationContactDetails(
                contactDetailsMapper.to(representative.getOrganisationContactDetails().orElse(null))
            )
            .build();
    }

    @Override
    public uk.gov.hmcts.cmc.domain.models.legalrep.Representative from(Representative representative) {
        if (representative == null) {
            return null;
        }
        return new uk.gov.hmcts.cmc.domain.models.legalrep.Representative(
            representative.getOrganisationName(),
            addressMapper.from(representative.getOrganisationAddress()),
            contactDetailsMapper.from(representative.getOrganisationContactDetails()));
    }
}
