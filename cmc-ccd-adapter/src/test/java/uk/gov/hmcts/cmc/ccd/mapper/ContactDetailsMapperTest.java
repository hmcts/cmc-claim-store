package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.mapper.util.AssertUtil.assertContactDetailsEqualTo;

public class ContactDetailsMapperTest {

    private static final String PHONE = "07987654321";
    private static final String EMAIL = "my@email.com";
    private static final String DX = "dx123";

    ContactDetailsMapper mapper = new ContactDetailsMapper();

    @Test
    public void shouldGetTargetNullIfSourceIsNullForTo() {
        //given
        ContactDetails contactDetails = null;

        //when
        uk.gov.hmcts.cmc.ccd.domain.ContactDetails ccdContactDetails = mapper.to(contactDetails);

        //then
        assertThat(ccdContactDetails).isNull();
    }

    @Test
    public void shouldMapContactDetailsToCCD() {
        //given
        ContactDetails contactDetails = new ContactDetails(PHONE, EMAIL, DX);

        //when
        uk.gov.hmcts.cmc.ccd.domain.ContactDetails ccdContactDetails = mapper.to(contactDetails);

        //then
        assertThat(ccdContactDetails).isNotNull();
        assertThat(ccdContactDetails.getEmail()).isEqualTo(EMAIL);
        assertThat(ccdContactDetails.getPhone()).isEqualTo(PHONE);
        assertThat(ccdContactDetails.getDxAddress()).isEqualTo(DX);

    }

    @Test
    public void shouldGetTargetNullIfSourceIsNullForFrom() {
        //given
        uk.gov.hmcts.cmc.ccd.domain.ContactDetails contactDetails = null;

        //when
        ContactDetails cmcContactDetails = mapper.from(contactDetails);

        //then
        assertThat(cmcContactDetails).isNull();
    }

    @Test
    public void shouldMapContactDetailsToCMC() {
        //given
        uk.gov.hmcts.cmc.ccd.domain.ContactDetails contactDetails = uk.gov.hmcts.cmc.ccd.domain.ContactDetails.builder()
            .phone(PHONE)
            .email(EMAIL)
            .dxAddress(DX)
            .build();

        //when
        ContactDetails cmcContactDetails = mapper.from(contactDetails);

        //then
        assertThat(cmcContactDetails).isNotNull();
        assertThat(cmcContactDetails.getPhone().isPresent());
        assertThat(cmcContactDetails.getEmail().isPresent());
        assertThat(cmcContactDetails.getDxAddress().isPresent());

        assertContactDetailsEqualTo(cmcContactDetails, contactDetails);
    }
}
