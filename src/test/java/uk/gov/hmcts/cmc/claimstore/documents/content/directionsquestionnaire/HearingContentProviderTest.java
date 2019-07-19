package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.HearingContent;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HearingContentProviderTest {

    private HearingContentProvider hearingContentProvider = new HearingContentProvider();
    private final String disabledAccess = "Disabled Access";
    private final String yes = "YES";
    private final String no = "NO";

    @Test(expected = IllegalArgumentException.class)
    public void mapDirectionsQuestionnaireThrowsException() {
        hearingContentProvider.mapDirectionQuestionnaire.apply(null);
    }

    @Test
    public void mapDirectionsQuestionnaireMapsHearingContent() {
        DirectionsQuestionnaire dq = SampleDirectionsQuestionnaire.builder().build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire.apply(dq);

        dq.getWitness().ifPresent(
            witness -> assertEquals(witness, hearingContent.getWitness())
        );

        dq.getRequireSupport()
            .ifPresent(reqSupport -> compareSupportRequired(reqSupport, hearingContent.getSupportRequired()));

        dq.getExpertRequest()
            .ifPresent(request -> {
                assertEquals(yes, hearingContent.getHasExpertReport());
                assertEquals(yes, hearingContent.getCourtPermissionForExpertReport());
                assertEquals(request.getReasonForExpertAdvice(), hearingContent.getReasonWhyExpertAdvice());
                assertEquals(request.getExpertEvidenceToExamine(), hearingContent.getExpertExamineNeeded());
            });
        assertEquals(dq.getHearingLocation().getCourtName(), hearingContent.getHearingLocation());
        assertArrayEquals(dq.getExpertReports().toArray(), hearingContent.getExpertReports().toArray());
        assertArrayEquals(
            unavailabeDatesToISOString(dq.getUnavailableDates()),
            hearingContent.getUnavailableDates().toArray()
        );

        if (dq.getExpertReports().isEmpty()) {
            assertEquals(no, hearingContent.getHasExpertReport());
        } else {
            assertEquals(yes, hearingContent.getHasExpertReport());
        }
    }

    @Test
    public void mapDirectionsQuestionnaireDontFailWhenOptionalIsEmpty() {
        DirectionsQuestionnaire dq = DirectionsQuestionnaire.builder()
            .hearingLocation(SampleHearingLocation.defaultHearingLocation.get())
            .build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire.apply(dq);

        assertThat(hearingContent.getExpertReports(), Matchers.is(Matchers.empty()));
        assertThat(hearingContent.getUnavailableDates(), Matchers.is(Matchers.empty()));

    }

    private void compareSupportRequired(RequireSupport supportRequired, List<String> mappedSupport) {
        supportRequired.getDisabledAccess()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(disabledAccess)));
        supportRequired.getOtherSupport()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
        supportRequired.getSignLanguageInterpreter()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
        supportRequired.getLanguageInterpreter()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(val)));
    }

    private Object[] unavailabeDatesToISOString(List<UnavailableDate> unavailableDates) {
        return unavailableDates
            .stream()
            .map(UnavailableDate::getUnavailableDate)
            .map(DateUtils::toISOFullStyle)
            .toArray();
    }
}
