package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimSubmissionOperationIndicators.withAllOperationDefaulted;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimSubmissionOperationIndicators.withAllOperationSuccess;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimSubmissionOperationIndicatorMapperTest {

    @Autowired
    private ClaimSubmissionOperationIndicatorMapper claimSubmissionOperationIndicatorMapper;

    @Test
    public void shouldMapToAndFromCCDForAllFieldsMarkedYes() {
        //given
        ClaimSubmissionOperationIndicators input = withAllOperationSuccess.get();
        //when
        CCDClaimSubmissionOperationIndicators to = claimSubmissionOperationIndicatorMapper.to(input);
        ClaimSubmissionOperationIndicators from = claimSubmissionOperationIndicatorMapper.from(to);
        //then
        assertThat(input).isEqualTo(from);
        assertThat(from.getRpa()).isEqualTo(YES);
        assertThat(from.getDefendantNotification()).isEqualTo(YES);
        assertThat(from.getClaimIssueReceiptUpload()).isEqualTo(YES);
    }

    @Test
    public void shouldMapToAndFromCCDForAllFieldsDefaulted() {
        //given
        ClaimSubmissionOperationIndicators input = withAllOperationDefaulted.get();
        //when
        CCDClaimSubmissionOperationIndicators to = claimSubmissionOperationIndicatorMapper.to(input);
        ClaimSubmissionOperationIndicators from = claimSubmissionOperationIndicatorMapper.from(to);
        //then
        assertThat(input).isEqualTo(from);
        assertThat(from.getRpa()).isEqualTo(NO);
        assertThat(from.getDefendantNotification()).isEqualTo(NO);
        assertThat(from.getClaimIssueReceiptUpload()).isEqualTo(NO);
    }

    @Test
    public void shouldMapToAndFromAndIgnoreNullFields() {
        //given
        ClaimSubmissionOperationIndicators input = ClaimSubmissionOperationIndicators.builder().build();
        //when
        CCDClaimSubmissionOperationIndicators to = claimSubmissionOperationIndicatorMapper.to(input);
        ClaimSubmissionOperationIndicators from = claimSubmissionOperationIndicatorMapper.from(to);
        //then
        assertThat(input).isEqualTo(from);
        assertThat(from.getRpa()).isNull();
        assertThat(from.getDefendantNotification()).isNull();
        assertThat(from.getClaimIssueReceiptUpload()).isNull();
    }
}
