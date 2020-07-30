package uk.gov.hmcts.cmc.ccd.mapper;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDProceedOnPaperReasonType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.ccd.util.MapperUtil;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ProceedOfflineReasonType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDClaimSubmissionOperationIndicators.CCDClaimSubmissionOperationIndicatorsWithPinSuccess;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDClaimSubmissionOperationIndicators.defaultCCDClaimSubmissionOperationIndicators;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper ccdCaseMapper;

    private static final String PREFERRED_COURT  = "preferred-court";

    @Test
    public void shouldMapLegalClaimToCCD() {
        //given
        Claim claim = SampleClaim.getLegalDataWithReps()
            .toBuilder().state(null).build();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
        assertEquals(NO, ccdCase.getMigratedFromClaimStore());
        assertEquals(NO, ccdCase.getApplicants().get(0).getValue().getLeadApplicantIndicator());
        assertEquals(MapperUtil.toCaseName.apply(claim), ccdCase.getCaseName());
    }

    @Test
    public void shouldMapCitizenClaimToCCD() {
        //given
        Claim claim = SampleClaim.getCitizenClaim()
            .toBuilder().state(null).build();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
        assertEquals(NO, ccdCase.getMigratedFromClaimStore());
        assertEquals(YES, ccdCase.getApplicants().get(0).getValue().getLeadApplicantIndicator());
        assertEquals(MapperUtil.toCaseName.apply(claim), ccdCase.getCaseName());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromClaim() {
        //given
        Claim claim = SampleClaim.builder().withClaimData(null).build();

        //when
        ccdCaseMapper.to(claim);
    }

    private CCDCase getCcdCaseWithClaimantProvidedEmail(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        String email = respondent.getPartyDetail().getEmailAddress();
        CCDParty detailByClaimant = respondent.getClaimantProvidedDetail().toBuilder().emailAddress(email).build();
        CCDRespondent individualRespondent = respondent.toBuilder().claimantProvidedDetail(detailByClaimant).build();
        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(individualRespondent).build());
        return ccdCase.toBuilder().respondents(respondents).build();
    }

    @Test
    public void shouldMapLegalClaimFromCCD() {
        //given
        CCDCase ccdCase = SampleData.getCCDLegalCase();

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(getCcdCaseWithClaimantProvidedEmail(ccdCase));
        assertEquals(MapperUtil.getMediationOutcome(ccdCase), claim.getMediationOutcome().orElse(null));
    }

    @Test
    public void shouldMapCitizenClaimFromCCD() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown());

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(getCcdCaseWithClaimantProvidedEmail(ccdCase));
        assertEquals(ClaimState.OPEN, claim.getState());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromCCDCase() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(null);

        //when
        ccdCaseMapper.from(ccdCase);
    }

    @Test
    public void shouldMapClaimSubmissionIndicatorsFromCCDCase() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown());

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertNotNull(claim.getClaimSubmissionOperationIndicators());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimIssueReceiptUpload());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getBulkPrint());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getDefendantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getRpa());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getSealedClaimUpload());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getStaffNotification());
    }

    @Test
    public void shouldMapSubmissionIndicatorsFromCCDCaseWithDefaultIndicators() {
        //given
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(defaultCCDClaimSubmissionOperationIndicators);

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertNotNull(claim.getClaimSubmissionOperationIndicators());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimIssueReceiptUpload());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getBulkPrint());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getDefendantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getRpa());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getSealedClaimUpload());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getStaffNotification());
    }

    @Test(expected = NullPointerException.class)
    public void shouldMapSubmissionIndicatorsFromCCDCaseWithNullIndicators() {
        //given
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(null);

        //when
        ccdCaseMapper.from(ccdCase);
    }

    @Test
    public void shouldMapSubmissionIndicatorsFromCCDCaseWithPinSuccessIndicators() {
        //given
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(CCDClaimSubmissionOperationIndicatorsWithPinSuccess);

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertNotNull(claim.getClaimSubmissionOperationIndicators());
        assertEquals(YesNoOption.YES, claim.getClaimSubmissionOperationIndicators().getBulkPrint());
        assertEquals(YesNoOption.YES, claim.getClaimSubmissionOperationIndicators().getStaffNotification());
        assertEquals(YesNoOption.YES, claim.getClaimSubmissionOperationIndicators().getDefendantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimIssueReceiptUpload());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getClaimantNotification());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getRpa());
        assertEquals(YesNoOption.NO, claim.getClaimSubmissionOperationIndicators().getSealedClaimUpload());
    }

    @Test
    public void shouldMapDirectionOrderCreatedOnFromCCDCase() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown()).toBuilder()
            .directionOrder(CCDDirectionOrder.builder()
                .createdOn(LocalDateTime.now())
                .hearingCourtAddress(SampleData.getCCDAddress())
                .build())
            .build();

        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertTrue(claim.getDirectionOrder().isPresent());
        assertThat(claim.getDirectionOrder().get()).isEqualTo(ccdCase.getDirectionOrder());
    }

    @Test
    public void shouldReverseMapProceedOnPaperDetails() {
        String otherReason = "Judge want it to be moved";
        CCDCase ccdCase = SampleData.getCCDLegalCase().toBuilder()
            .proceedOnPaperReason(CCDProceedOnPaperReasonType.OTHER)
            .proceedOnPaperOtherReason(otherReason)
            .build();

        Claim claim = ccdCaseMapper.from(ccdCase);

        assertEquals(ccdCase.getProceedOnPaperReason().name(), claim.getProceedOfflineReason().get().name());
        assertEquals(ccdCase.getProceedOnPaperOtherReason(), claim.getProceedOfflineOtherReasonDescription());
    }

    @Test
    public void shouldMapProceedOnPaperDetails() {
        String otherReason = "Judge want it to be moved";
        Claim claim = SampleClaim.getDefault().toBuilder()
            .proceedOfflineReason(ProceedOfflineReasonType.APPLICATION_BY_CLAIMANT)
            .proceedOfflineOtherReasonDescription(otherReason)
            .build();

        CCDCase ccdCase = ccdCaseMapper.to(claim);

        assertEquals(claim.getProceedOfflineReason().get().name(), ccdCase.getProceedOnPaperReason().name());
        assertEquals(claim.getProceedOfflineOtherReasonDescription(), ccdCase.getProceedOnPaperOtherReason());
    }

    @Test
    public void shouldMapPreferredDQCourtOnFromCCDCase() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown()).toBuilder()
            .preferredDQCourt(PREFERRED_COURT)
            .build();

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        MatcherAssert.assertThat(claim.getPreferredDQCourt().get(), is(PREFERRED_COURT));
    }
}
