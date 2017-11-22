package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ContactDetailsMapperTest {

    private static final String PHONE = "07987654321";
    private static final String EMAIL = "my@email.com";
    private static final String DX = "dx123";

    ContactDetailsMapper mapper = new ContactDetailsMapper();

    @Test
    public void shouldMapContactDetailsToCCD() {
        //given
        ContactDetails contactDetails = new ContactDetails(PHONE, EMAIL, DX);

        //when
        CCDContactDetails ccdContactDetails = mapper.to(contactDetails);

        //then
        assertThat(contactDetails).isEqualTo(ccdContactDetails);

    }

    @Test
    public void shouldMapContactDetailsToCMC() {
        //given
        CCDContactDetails contactDetails = CCDContactDetails.builder()
            .phone(PHONE)
            .email(EMAIL)
            .dxAddress(DX)
            .build();

        //when
        ContactDetails cmcContactDetails = mapper.from(contactDetails);

        //then
        assertThat(cmcContactDetails).isEqualTo(contactDetails);
    }
}
