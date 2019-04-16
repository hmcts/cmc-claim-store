package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseSubmitClaimTest;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeline;

import java.util.function.Supplier;

import static java.util.Arrays.asList;

public class SubmitClaimTest extends BaseSubmitClaimTest {

    @Before
    public void before() {
        user = idamTestService.createCitizen();
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidTimelineIsSubmitted() {
        ClaimData invalidClaimData = testData.submittedByClaimantBuilder()
            .withTimeline(SampleTimeline.builder().withEvents(asList(new TimelineEvent[1001])).build())
            .build();

        submitClaim(invalidClaimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidEvidenceIsSubmitted() {
        ClaimData invalidClaimData = testData.submittedByClaimantBuilder()
            .withEvidence(SampleEvidence.builder().withRows(asList(new EvidenceRow[1001])).build())
            .build();

        submitClaim(invalidClaimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

}
