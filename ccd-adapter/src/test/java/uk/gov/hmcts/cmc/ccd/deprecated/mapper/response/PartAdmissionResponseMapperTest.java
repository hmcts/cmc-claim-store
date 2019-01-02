package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPartAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.SampleData.getCCDDefendantIndividual;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDStatementOfTruth;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PartAdmissionResponseMapperTest {

    @Autowired
    private PartAdmissionResponseMapper mapper;

    @Test
    public void shouldMapPartAdmissionResponseImmediatePaymentToCCD() {
        //given
        PartAdmissionResponse partAdmissionResponse = PartAdmissionResponse.builder()
            .freeMediation(NO)
            .moreTimeNeeded(NO)
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfTruth(StatementOfTruth.builder().signerName("Name").signerRole("A role").build())
            .build();

        //when
        CCDPartAdmissionResponse ccdPartAdmissionResponse = mapper.to(partAdmissionResponse);

        //then
        // assertThat(partAdmissionResponse).isEqualTo(ccdPartAdmissionResponse);
    }

    @Test
    public void shouldMapPartAdmissionResponseStatementOfMeansToCCD() {
        //given
        PartAdmissionResponse partAdmissionResponse = PartAdmissionResponse.builder()
            .moreTimeNeeded(NO)
            .freeMediation(NO)
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .statementOfMeans(SampleStatementOfMeans.builder().build())
            .statementOfTruth(StatementOfTruth.builder().signerName("Name").signerRole("A role").build())
            .build();

        //when
        CCDPartAdmissionResponse ccdPartAdmissionResponse = mapper.to(partAdmissionResponse);

        //then
        //assertThat(partAdmissionResponse).isEqualTo(ccdPartAdmissionResponse);
    }

    @Test
    public void shouldMapPartAdmissionResponseFromCCD() {
        //given
        CCDPartAdmissionResponse ccdPartAdmissionResponse = CCDPartAdmissionResponse.builder()
            .freeMediationOption(CCDYesNoOption.YES)
            .moreTimeNeededOption(CCDYesNoOption.YES)
            .paymentIntention(CCDPaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(LocalDate.now().plusDays(7))
                .build())
            .defendant(getCCDDefendantIndividual())
            .statementOfTruth(getCCDStatementOfTruth())
            .statementOfTruth(CCDStatementOfTruth.builder().signerName("Name").signerRole("A role").build())
            .build();

        //when
        PartAdmissionResponse partAdmissionResponse = mapper.from(ccdPartAdmissionResponse);

        //then
        //assertThat(partAdmissionResponse).isEqualTo(ccdPartAdmissionResponse);
    }

}
