package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContactDetailsMapperTest {

    private static final String PHONE = "07987654321";
    private static final String EMAIL = "my@email.com";
    private static final String DX = "dx123";

    Mapper<uk.gov.hmcts.cmc.ccd.domain.ContactDetails, ContactDetails> mapper = new ContactDetailsMapper();

    @Test
    public void shouldMapContactDetailsToCCD() {
        //given
        ContactDetails contactDetails = new ContactDetails(PHONE, EMAIL, DX);

        //when
        uk.gov.hmcts.cmc.ccd.domain.ContactDetails ccdContactDetails = mapper.to(contactDetails);

        //then
        assertNotNull(ccdContactDetails);
        assertEquals(ccdContactDetails.getEmail(), EMAIL);
        assertEquals(ccdContactDetails.getPhone(), PHONE);
        assertEquals(ccdContactDetails.getDxAddress(), DX);

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
        assertNotNull(cmcContactDetails);
        assertTrue(cmcContactDetails.getPhone().isPresent());
        assertTrue(cmcContactDetails.getEmail().isPresent());
        assertTrue(cmcContactDetails.getDxAddress().isPresent());
        assertEquals(cmcContactDetails.getPhone().orElse(null), contactDetails.getPhone());
        assertEquals(cmcContactDetails.getEmail().orElse(null), contactDetails.getEmail());
        assertEquals(cmcContactDetails.getDxAddress().orElse(null), contactDetails.getDxAddress());
    }
}
