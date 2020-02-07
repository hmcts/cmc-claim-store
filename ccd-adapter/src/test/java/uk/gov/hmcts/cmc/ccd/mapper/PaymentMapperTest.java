package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PaymentMapperTest {

    @Autowired
    private PaymentMapper mapper;

    @Test
    public void shouldMapPaymentToCCD() {
        //given
        Payment payment = SamplePayment.builder().build();

        //when
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();
        mapper.to(payment, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        assertThat(LocalDate.parse(payment.getDateCreated(), ISO_DATE))
            .isEqualTo(ccdCase.getPaymentDateCreated());
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.fromValue(ccdCase.getPaymentStatus()));
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
    }

    @Test
    public void shouldMapPaymentToCCDWhenNoCreatedDateProvided() {
        //given
        Payment payment = SamplePayment.builder().dateCreated(null).build();

        //when
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();
        mapper.to(payment, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        assertThat(ccdCase.getPaymentDateCreated()).isNull();
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.fromValue(ccdCase.getPaymentStatus()));
    }

    @Test
    public void shouldMapPaymentToCCDWhenLongCreatedDateProvided() {
        //given
        Payment payment = SamplePayment.builder().dateCreated("1511169381890").build();

        //when
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();
        mapper.to(payment, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        assertThat(LocalDateTimeFactory.fromLong(Long.valueOf(payment.getDateCreated())))
            .isEqualTo(ccdCase.getPaymentDateCreated());
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.fromValue(ccdCase.getPaymentStatus()));
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
    }

    @Test
    public void shouldMapPaymentFromCCD() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .paymentAmount("400000")
            .paymentReference("RC-1524-6488-1670-7520")
            .paymentId("PaymentId")
            .paymentNextUrl("http://nexturl.test")
            .paymentStatus("success")
            .paymentDateCreated(LocalDate.of(2019, 1, 1))
            .build();

        //when
        Payment payment = mapper.from(ccdCase);

        //then
        assertThat(LocalDate.parse(payment.getDateCreated(), ISO_DATE))
            .isEqualTo(ccdCase.getPaymentDateCreated());
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.fromValue(ccdCase.getPaymentStatus()));
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
    }

    @Test
    public void shouldMapPaymentFromCCDIfPaymentStatusIsNull() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .paymentAmount("400000")
            .paymentReference("RC-1524-6488-1670-7520")
            .paymentId("PaymentId")
            .paymentNextUrl("http://nexturl.test")
            .paymentStatus(null)
            .paymentDateCreated(LocalDate.of(2019, 1, 1))
            .build();

        //when
        Payment payment = mapper.from(ccdCase);

        //then
        assertThat(LocalDate.parse(payment.getDateCreated(), ISO_DATE))
            .isEqualTo(ccdCase.getPaymentDateCreated());
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getStatus()).isNull();
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
    }

    @Test
    public void shouldMapPaymentFromCCDWhenNoDateCreatedProvided() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .paymentAmount("400000")
            .paymentReference("RC-1524-6488-1670-7520")
            .paymentId("PaymentId")
            .paymentStatus("success")
            .paymentDateCreated(null)
            .build();

        //when
        Payment payment = mapper.from(ccdCase);

        //then
        assertThat(payment.getDateCreated()).isBlank();
        assertThat(payment.getId()).isEqualTo(ccdCase.getPaymentId());
        assertMoney(payment.getAmount()).isEqualTo(ccdCase.getPaymentAmount());
        assertThat(payment.getReference()).isEqualTo(ccdCase.getPaymentReference());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.fromValue(ccdCase.getPaymentStatus()));
        assertThat(payment.getNextUrl()).isEqualTo(ccdCase.getPaymentNextUrl());
    }
}
