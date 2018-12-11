package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartyIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDStatementOfTruth;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FullAdmissionResponseMapperTest {

    @Autowired
    private FullAdmissionResponseMapper mapper;

    @Test
    public void shouldMapFullAdmissionResponseImmediatePaymentToCCD() {
        //given
        FullAdmissionResponse fullAdmissionResponse = FullAdmissionResponse.builder()
            .moreTimeNeeded(NO)
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfTruth(StatementOfTruth.builder().signerName("Name").signerRole("A role").build())
            .build();

        //when
        CCDFullAdmissionResponse ccdFullAdmissionResponse = mapper.to(fullAdmissionResponse);

        //then
        assertThat(fullAdmissionResponse).isEqualTo(ccdFullAdmissionResponse);
    }

    @Test
    public void shouldMapFullAdmissionResponseStatementOfMeansToCCD() {
        //given
        FullAdmissionResponse fullAdmissionResponse = FullAdmissionResponse.builder()
            .moreTimeNeeded(NO)
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .statementOfMeans(SampleStatementOfMeans.builder().build())
            .statementOfTruth(StatementOfTruth.builder().signerName("Name").signerRole("A role").build())
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
            .freeMediationOption(CCDYesNoOption.YES)
            .paymentIntention(CCDPaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(LocalDate.now().plusDays(7))
                .build())
            .defendant(getCCDPartyIndividual())
            .statementOfTruth(getCCDStatementOfTruth())
            .statementOfTruth(CCDStatementOfTruth.builder().signerName("Name").signerRole("A role").build())
            .build();

        //when
        FullAdmissionResponse fullAdmissionResponse = mapper.from(ccdFullAdmissionResponse);

        //then
        assertThat(fullAdmissionResponse).isEqualTo(ccdFullAdmissionResponse);
    }

}
