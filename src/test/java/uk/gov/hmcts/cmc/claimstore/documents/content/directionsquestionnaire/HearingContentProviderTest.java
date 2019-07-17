package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.HearingContent;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation;

import java.util.List;

public class HearingContentProviderTest {

    private HearingContentProvider hearingContentProvider = new HearingContentProvider();

    @Test(expected = IllegalArgumentException.class)
    public void mapDirectionsQuestionnaireThrowsException() {
        hearingContentProvider.mapDirectionQuestionnaire.apply(null);
    }

    @Test
    public void mapDirectionsQuestionnaireMapsHearingContent() {
        DirectionsQuestionnaire dq = SampleDirectionsQuestionnaire.builder().build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire.apply(dq);

        dq.getWitness().ifPresent(
            witness -> Assert.assertEquals(witness, hearingContent.getWitness())
        );

        dq.getRequireSupport()
            .ifPresent(reqSupport -> compareSupportRequired(reqSupport, hearingContent.getSupportRequired()));

        dq.getExpertRequest()
            .ifPresent(request -> {
                Assert.assertEquals("YES", hearingContent.getHasExpertReport());
                Assert.assertEquals("YES", hearingContent.getCourtPermissionForExpertReport());
                Assert.assertEquals(request.getReasonForExpertAdvice(), hearingContent.getReasonWhyExpertAdvice());
                Assert.assertEquals(request.getExpertEvidenceToExamine(), hearingContent.getExpertExamineNeeded());
            });
        Assert.assertEquals(dq.getHearingLocation().getCourtName(), hearingContent.getHearingLocation());
        Assert.assertArrayEquals(dq.getExpertReports().toArray(), hearingContent.getExpertReports().toArray());
        Assert.assertArrayEquals(dq.getUnavailableDates().toArray(), hearingContent.getUnavailableDates().toArray());

        if (dq.getExpertReports().isEmpty()) {
            Assert.assertEquals("NO", hearingContent.getHasExpertReport());
        } else {
            Assert.assertEquals("YES", hearingContent.getHasExpertReport());
        }
    }

    @Test
    public void mapDirectionsQuestionnaireDontFailWhenOptionalIsEmpty() {
        DirectionsQuestionnaire dq = DirectionsQuestionnaire.builder()
            .hearingLocation(SampleHearingLocation.defaultHearingLocation.get())
            .build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire.apply(dq);

        Assert.assertThat(hearingContent.getExpertReports(), Matchers.is(Matchers.empty()));
        Assert.assertThat(hearingContent.getUnavailableDates(), Matchers.is(Matchers.empty()));

    }

    private void compareSupportRequired(RequireSupport supportRequired, List<String> mappedSupport) {
        supportRequired.getDisabledAccess()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(hearingContentProvider.DISABLED_ACCESS)));
        supportRequired.getOtherSupport()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
        supportRequired.getSignLanguageInterpreter()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
        supportRequired.getLanguageInterpreter()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
    }
}
