package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ExpertReportContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.HearingContent;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class HearingContentProvider {

    private static final String DISABLED_ACCESS = "Disabled Access";
    private static final String HEARING_LOOP = "Hearing Loop";
    private static final String YES = "Yes";
    private static final String NO = "No";

    private Function<ExpertReport, ExpertReportContent> mapExpertReport = report ->
        ExpertReportContent.builder().expertName(report.getExpertName())
            .expertReportDate(Formatting.formatDate(report.getExpertReportDate()))
            .build();

    private Function<UnavailableDate, String> mapToISOFullStyle = unavailableDate ->
        Optional.ofNullable(unavailableDate)
            .map(UnavailableDate::getUnavailableDate)
            .map(DateUtils::toISOFullStyle).orElse("");

    private void mapHearingLocationDetails(HearingLocation hearingLocation,
                                           HearingContent.HearingContentBuilder contentBuilder) {
        contentBuilder.hearingLocation(hearingLocation.getCourtName());
        contentBuilder.locationReason(hearingLocation.getExceptionalCircumstancesReason()
            .orElse(""));
    }

    private void mapSupportRequirement(RequireSupport support,
                                       HearingContent.HearingContentBuilder builder) {

        List<String> supportNeeded = new ArrayList<>();
        support.getLanguageInterpreter().ifPresent(supportNeeded::add);
        support.getSignLanguageInterpreter().ifPresent(supportNeeded::add);
        support.getHearingLoop()
            .map(YesNoOption::name)
            .filter(hearingLoop -> hearingLoop.equals(YesNoOption.YES.name()))
            .ifPresent(x -> supportNeeded.add(HEARING_LOOP));
        support.getDisabledAccess()
            .map(YesNoOption::name)
            .filter(access -> access.equals(YesNoOption.YES.name()))
            .ifPresent(x -> supportNeeded.add(DISABLED_ACCESS));
        support.getOtherSupport().ifPresent(supportNeeded::add);
        builder.supportRequired(supportNeeded);

    }

    private void mapExpertRequest(ExpertRequest expertRequest, HearingContent.HearingContentBuilder builder) {

        YesNoOption expertRequired = expertRequest.getExpertRequired();

        builder.expertRequired(expertRequired.toString());
        if (expertRequired.equals(YesNoOption.YES)) {
            if (!StringUtils.isBlank(expertRequest.getReasonForExpertAdvice().orElse(""))) {
                expertRequest.getReasonForExpertAdvice().ifPresent(builder::reasonWhyExpertAdvice);
                builder.courtPermissionForExpertReport(YES);
                builder.expertExamineNeeded(YES);
            } else {
                builder.expertExamineNeeded(NO);
            }
            expertRequest.getExpertEvidenceToExamine().ifPresent(builder::whatToExamine);
        }
    }

    public HearingContent mapDirectionQuestionnaire(DirectionsQuestionnaire questionnaire) {

        requireNonNull(questionnaire, "Directions Questionnaire cant be null");

        HearingContent.HearingContentBuilder contentBuilder = HearingContent.builder();

        questionnaire.getRequireSupport()
            .ifPresent(support -> mapSupportRequirement(support, contentBuilder));

        questionnaire.getHearingLocation().ifPresent(hearingLocation -> mapHearingLocationDetails(hearingLocation,
            contentBuilder));

        contentBuilder.hasExpertReport(questionnaire.getExpertReports().isEmpty() ? NO : YES);

        questionnaire.getWitness().ifPresent(contentBuilder::witness);

        questionnaire.getExpertRequest().ifPresent(expertRequest -> mapExpertRequest(expertRequest, contentBuilder));

        contentBuilder.unavailableDates(
            questionnaire.getUnavailableDates().stream().map(mapToISOFullStyle).collect(toList())
        );

        contentBuilder.expertReports(asStream(questionnaire.getExpertReports()).map(mapExpertReport)
            .collect(toList()));

        return contentBuilder.build();
    }

}
