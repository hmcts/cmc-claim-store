package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartyIndividual;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FullAdmissionResponseMapperTest {

    @Autowired
    private FullAdmissionResponseMapper mapper;

    @Test
    public void shouldMapFullAdmissionResponseToCCD() {
        //given
        FullAdmissionResponse fullAdmissionResponse = FullAdmissionResponse.builder()
            .moreTimeNeeded(YesNoOption.NO)
            .paymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(7))
            .defendant(SampleParty.builder().individual())
            .build();

        //when
        CCDFullAdmissionResponse ccdFullAdmissionResponse = mapper.to(fullAdmissionResponse);

        //then
        assertThat(fullAdmissionResponse).isEqualTo(ccdFullAdmissionResponse);
    }

    @Test
    public void shouldMapFullAdmissionResponseFromCCD() {
        //given
        CCDFullAdmissionResponse ccdFullAdmissionResponse = CCDFullAdmissionResponse.builder()
            .moreTimeNeededOption(CCDYesNoOption.YES)
            .paymentOption(CCDPaymentOption.FULL_BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(7))
            .defendant(getCCDPartyIndividual())
            .build();

        //when
        FullAdmissionResponse fullAdmissionResponse = mapper.from(ccdFullAdmissionResponse);

        //then
        assertThat(fullAdmissionResponse).isEqualTo(ccdFullAdmissionResponse);
    }

}
