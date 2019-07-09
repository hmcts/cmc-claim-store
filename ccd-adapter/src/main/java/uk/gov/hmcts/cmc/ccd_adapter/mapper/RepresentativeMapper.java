package uk.gov.hmcts.cmc.ccd_adapter.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class RepresentativeMapper
    implements BuilderMapper<CCDApplicant, Representative, CCDApplicant.CCDApplicantBuilder> {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;

    @Autowired
    public RepresentativeMapper(AddressMapper addressMapper, ContactDetailsMapper contactDetailsMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
    }

    @Override
    public void to(Representative representative, CCDApplicant.CCDApplicantBuilder builder) {

        representative.getOrganisationContactDetails()
            .ifPresent(organisationContactDetails ->
                contactDetailsMapper.to(organisationContactDetails, builder));

        builder
            .representativeOrganisationName(representative.getOrganisationName())
            .representativeOrganisationAddress(addressMapper.to(representative.getOrganisationAddress()));
    }

    @Override
    public Representative from(CCDApplicant applicant) {
        if (isBlank(applicant.getRepresentativeOrganisationName())
            && applicant.getRepresentativeOrganisationAddress() == null
            && isBlank(applicant.getRepresentativeOrganisationEmail())
            && isBlank(applicant.getRepresentativeOrganisationPhone())
            && isBlank(applicant.getRepresentativeOrganisationDxAddress())
        ) {
            return null;
        }

        return new Representative(
            applicant.getRepresentativeOrganisationName(),
            addressMapper.from(applicant.getRepresentativeOrganisationAddress()),
            contactDetailsMapper.from(applicant)
        );

    }
}
