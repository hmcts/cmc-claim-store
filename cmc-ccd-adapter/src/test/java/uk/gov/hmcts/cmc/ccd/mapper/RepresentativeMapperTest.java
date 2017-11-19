package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class RepresentativeMapperTest {

    private AddressMapper addressMapper = new AddressMapper();
    private ContactDetailsMapper contactDetailsMapper = new ContactDetailsMapper();
    private RepresentativeMapper representativeMapper = new RepresentativeMapper(addressMapper, contactDetailsMapper);

    @Test
    public void shouldMapRepresentativeToCCD() {
        //given
        Representative representative = SampleRepresentative.builder().build();

        //when
        CCDRepresentative ccdRepresentative = representativeMapper.to(representative);

        //then
        assertThat(ccdRepresentative).isEqualTo(representative);
    }

    @Test
    public void shouldMapRepresentativeToCMC() {
        //given
        final CCDContactDetails contactDetails = CCDContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
        final CCDAddress address = CCDAddress.builder()
            .line1("line 1")
            .line2("line 2")
            .city("city")
            .postcode("postcode")
            .build();
        CCDRepresentative representative = CCDRepresentative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(contactDetails)
            .organisationAddress(address)
            .build();

        //when
        Representative cmcRepresentative = representativeMapper.from(representative);

        //then
        assertThat(representative).isEqualTo(cmcRepresentative);
    }

}
