package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentDeclaration;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentDeclarationMapper;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PaymentDeclarationMapperTest {

    private final LocalDate paidDate = LocalDate.of(2017, 12, 31);
    private final String explanation = "Payed by card";

    @Autowired
    private PaymentDeclarationMapper mapper;

    @Test
    public void shouldMapToCCD() {
        //given
        PaymentDeclaration cmcInstance = new PaymentDeclaration(paidDate, explanation);
        //when
        CCDPaymentDeclaration ccdInstance = mapper.to(cmcInstance);
        //then
        assertThat(cmcInstance).isEqualTo(ccdInstance);
    }

    @Test
    public void shouldMapFromCCD() {
        //given
        CCDPaymentDeclaration ccdInstance = CCDPaymentDeclaration
            .builder()
            .paidDate(paidDate)
            .explanation(explanation)
            .build();
        //when
        PaymentDeclaration cmcInstance = mapper.from(ccdInstance);
        //then
        assertThat(cmcInstance).isEqualTo(ccdInstance);
    }
}
