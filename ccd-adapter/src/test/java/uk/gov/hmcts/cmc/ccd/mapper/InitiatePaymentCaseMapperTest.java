package uk.gov.hmcts.cmc.ccd.mapper;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InitiatePaymentCaseMapperTest {
    @Autowired
    private InitiatePaymentCaseMapper mapper;

    @Test
    public void shouldMapInitiatePaymentToCCD() {
        InitiatePaymentRequest initiatePaymentRequest = InitiatePaymentRequest
            .builder()
            .amount(SampleAmountBreakdown.builder().build())
            .interest(SampleInterest.standard())
            .externalId(UUID.randomUUID())
            .build();

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        mapper.to(initiatePaymentRequest, builder);

        assertThat(initiatePaymentRequest).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapInitiatePaymentFromCCD() {
        InitiatePaymentRequest expectedPaymentRequest = buildExpectedPaymentRequest();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(
            ImmutableList.of(CCDCollectionElement.<CCDAmountRow>builder()
                .id("aaa")
                .value(CCDAmountRow.builder()
                    .reason("payment")
                    .amount("5000")
                    .build())
                .build())
        );
        ccdCase.setExternalId("ebc2dd5b-4f9b-4088-a40c-29f1e78c9d72");
        InitiatePaymentRequest initiatePaymentRequest = mapper.from(ccdCase);

        //then
        assertThat(initiatePaymentRequest).isEqualTo(expectedPaymentRequest);
    }

    private InitiatePaymentRequest buildExpectedPaymentRequest() {
        return InitiatePaymentRequest
            .builder()
            .amount(SampleAmountBreakdown.builder()
                .rows(ImmutableList.of(
                    AmountRow.builder()
                        .amount(new BigDecimal("50.00"))
                        .reason("payment")
                        .id("aaa")
                        .build()
                ))
                .build())
            .interest(SampleInterest.builder()
                .withReason("reason")
                .withRate(BigDecimal.valueOf(2))
                .withInterestBreakdown(SampleInterestBreakdown.builder()
                    .withTotalAmount(new BigDecimal("210.00"))
                    .withExplanation("Explanation")
                    .build())
                .withInterestDate(SampleInterestDate.builder()
                    .withDate(LocalDate.parse("2019-08-21"))
                    .withType(InterestDate.InterestDateType.CUSTOM)
                    .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
                    .withReason("start date reason")
                    .build())
                .withSpecificDailyAmount(new BigDecimal("10.00"))
                .build())
            .externalId(UUID.fromString("ebc2dd5b-4f9b-4088-a40c-29f1e78c9d72"))
            .build();
    }
}
