package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Optional;

@Component
public class RepresentativeMapper implements Mapper<CCDRepresentative, Representative> {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;

    @Autowired
    public RepresentativeMapper(final AddressMapper addressMapper, final ContactDetailsMapper contactDetailsMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
    }

    @Override
    public CCDRepresentative to(Representative representative) {

        final CCDRepresentative.CCDRepresentativeBuilder builder = CCDRepresentative.builder();
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
        final Optional<CCDContactDetails> organisationContactDetailsOptional =
            representative.getOrganisationContactDetails();
        final ContactDetails organisationContactDetails =
            organisationContactDetailsOptional
                .isPresent() ? contactDetailsMapper.from(organisationContactDetailsOptional.get()) : null;

        return new Representative(
            representative.getOrganisationName(),
            addressMapper.from(representative.getOrganisationAddress()),
            organisationContactDetails
        );
    }
}
