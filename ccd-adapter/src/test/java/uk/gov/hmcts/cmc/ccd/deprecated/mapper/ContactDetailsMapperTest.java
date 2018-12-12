package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ContactDetailsMapperTest {

    private static final String PHONE = "07987654321";
    private static final String EMAIL = "my@email.com";
    private static final String DX = "dx123";

    @Autowired
    private ContactDetailsMapper mapper;

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
