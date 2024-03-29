package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ExpertReportContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.HearingContent;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation.defaultHearingLocation;

public class HearingContentProviderTest {

    private final HearingContentProvider hearingContentProvider = new HearingContentProvider();
    private final String disabledAccess = "Disabled Access";
    private final String hearingLoop = "Hearing Loop";
    private final String yes = "Yes";
    private final String no = "No";

    @Test(expected = NullPointerException.class)
    public void mapDirectionsQuestionnaireThrowsException() {
        hearingContentProvider.mapDirectionQuestionnaire(null);
    }

    @Test
    public void mapDirectionsQuestionnaireNoneSupportRequired() {
        DirectionsQuestionnaire dq = SampleDirectionsQuestionnaire.builder().buildNone();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire(dq);
        assertEquals(2, hearingContent.getSupportRequired().size());

    }

    @Test
    public void mapDirectionsQuestionnaireExtraSupportRequired() {
        DirectionsQuestionnaire dq = DirectionsQuestionnaire.builder()
            .requireSupport(RequireSupport.builder()
                .languageInterpreter("English")
                .signLanguageInterpreter("Need Sign Language")
                .otherSupport("Extra Support")
                .disabledAccess(YES)
                .hearingLoop(YES)
                .build()
            ).build();

        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire(dq);
        assertEquals(5, hearingContent.getSupportRequired().size());

    }

    @Test
    public void mapDirectionsQuestionnaireMapsHearingContent() {
        DirectionsQuestionnaire dq = SampleDirectionsQuestionnaire.builder().build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire(dq);

        dq.getWitness().ifPresent(
            witness -> assertEquals(witness, hearingContent.getWitness())
        );

        dq.getRequireSupport()
            .ifPresent(reqSupport -> compareSupportRequired(reqSupport, hearingContent.getSupportRequired()));

        dq.getExpertRequired()
            .ifPresent(expertRequired -> assertEquals(yes, hearingContent.getExpertRequired()));

        dq.getPermissionForExpert()
            .ifPresent(permission -> assertEquals(yes, hearingContent.getCourtPermissionForExpertReport()));

        dq.getExpertRequest()
            .ifPresent(request -> {
                assertEquals(yes, hearingContent.getExpertExamineNeeded());
                assertEquals(yes, hearingContent.getCourtPermissionForExpertReport());
                assertEquals(request.getReasonForExpertAdvice(), hearingContent.getReasonWhyExpertAdvice());
                assertEquals(request.getExpertEvidenceToExamine(), hearingContent.getWhatToExamine());
            });
        dq.getHearingLocation().ifPresent(location ->
            assertEquals(location.getCourtName(), hearingContent.getHearingLocation())
        );

        dq.getVulnerabilityQuestions().ifPresent(
            vulnerabilityQuestions -> assertEquals(vulnerabilityQuestions, hearingContent.getVulnerabilityQuestions())
        );

        dq.getDeterminationWithoutHearingQuestions().ifPresent(
            determinationWithoutHearingQuestions -> assertEquals(determinationWithoutHearingQuestions,
            hearingContent.getDeterminationWithoutHearingQuestions())
        );

        compareExpertReport(dq.getExpertReports(), hearingContent.getExpertReports());
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
            .hearingLocation(defaultHearingLocation)
            .expertRequired(YesNoOption.YES)
            .build();
        HearingContent hearingContent = hearingContentProvider.mapDirectionQuestionnaire(dq);

        MatcherAssert.assertThat(hearingContent.getExpertReports(), Matchers.is(Matchers.empty()));
        MatcherAssert.assertThat(hearingContent.getUnavailableDates(), Matchers.is(Matchers.empty()));

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
        supportRequired.getHearingLoop()
            .ifPresent(val -> Assert.assertTrue(mappedSupport.contains(hearingLoop)));
    }

    private Object[] unavailabeDatesToISOString(List<UnavailableDate> unavailableDates) {
        return unavailableDates
            .stream()
            .map(UnavailableDate::getUnavailableDate)
            .map(DateUtils::toISOFullStyle)
            .toArray();
    }

    private void compareExpertReport(List<ExpertReport> report, List<ExpertReportContent> reportContents) {
        assertArrayEquals(report.stream().map(reportItem -> ExpertReportContent.builder()
            .expertName(reportItem.getExpertName())
            .expertReportDate(Formatting.formatDate(reportItem.getExpertReportDate()))
            .build())
            .toArray(), reportContents.toArray());
    }
}

