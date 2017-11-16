package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.Address;
import uk.gov.hmcts.cmc.ccd.domain.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.mapper.util.AssertUtil.assertRepresentativeEqualTo;

public class RepresentativeMapperTest {

    private AddressMapper addressMapper = new AddressMapper();
    private ContactDetailsMapper contactDetailsMapper = new ContactDetailsMapper();
    private RepresentativeMapper representativeMapper = new RepresentativeMapper(addressMapper, contactDetailsMapper);


    @Test
    public void shouldGetTargetNullIfSourceIsNullForTo() {
        //given
        Representative representative = null;

        //when
        uk.gov.hmcts.cmc.ccd.domain.Representative ccdRepresentative = representativeMapper.to(representative);
        //then
        assertThat(ccdRepresentative).isNull();
    }

    @Test
    public void shouldMapRepresentativeToCCD() {
        //given
        Representative representative = SampleRepresentative.builder().build();

        //when
        uk.gov.hmcts.cmc.ccd.domain.Representative ccdRepresentative = representativeMapper.to(representative);

        //then
        assertThat(ccdRepresentative).isNotNull();
        assertRepresentativeEqualTo(representative, ccdRepresentative);
    }

    @Test
    public void shouldGetTargetNullIfSourceIsNullForFrom() {
        //given
        uk.gov.hmcts.cmc.ccd.domain.Representative representative = null;

        //when
        Representative cmcRepresentative = representativeMapper.from(representative);
        //then
        assertThat(cmcRepresentative).isNull();
    }

    @Test
    public void shouldMapRepresentativeToCMC() {
        //given
        final ContactDetails contactDetails = ContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
        final Address address = Address.builder()
            .line1("line 1")
            .line2("line 2")
            .city("city")
            .postcode("postcode")
            .build();
        uk.gov.hmcts.cmc.ccd.domain.Representative representative = uk.gov.hmcts.cmc.ccd.domain.Representative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(contactDetails)
            .organisationAddress(address)
            .build();

        //when
        Representative cmcRepresentative = representativeMapper.from(representative);

        //then
        assertRepresentativeEqualTo(cmcRepresentative, representative);
    }

}
