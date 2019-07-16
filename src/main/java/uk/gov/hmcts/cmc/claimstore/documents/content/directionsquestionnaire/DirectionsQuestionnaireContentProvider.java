package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.HearingContent;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class DirectionsQuestionnaireContentProvider {

    private static BiConsumer<RequireSupport, HearingContent.HearingContentBuilder> mapSupportRequirement =
        (support, builder) -> {
            List<String> supportNeeded = new ArrayList<>();
            support.getLanguageInterpreter().ifPresent(supportNeeded::add);
            support.getSignLanguageInterpreter().ifPresent(supportNeeded::add);
            support.getOtherSupport().ifPresent(supportNeeded::add);
            support.getDisabledAccess()
                .map(YesNoOption::name)
                .filter(access -> access.equals(YesNoOption.YES.name()))
                .ifPresent(x -> supportNeeded.add("Disabled Access"));
            builder.supportRequired(supportNeeded);
        };

    private static BiConsumer<ExpertRequest, HearingContent.HearingContentBuilder> mapWitnessDetails =
        (expertRequest, builder) -> {
            builder.hasExpertReport("YES");
            builder.courtPermissionForExpertReport("YES");
            builder.reasonWhyExpertAdvice(expertRequest.getReasonForExpertAdvice());
            builder.expertExamineNeeded(expertRequest.getExpertEvidenceToExamine());
        };

    public Function<DirectionsQuestionnaire, HearingContent> mapDirectionQuestionnaire =
        questionnaire -> {
            HearingContent.HearingContentBuilder contentBuilder = HearingContent.builder();

            questionnaire.getExpertRequest()
                .map(ExpertRequest::getReasonForExpertAdvice)
                .ifPresent(contentBuilder::courtPermissionForExpertReport);

            questionnaire.getRequireSupport()
                .ifPresent(support -> mapSupportRequirement.accept(support, contentBuilder));

            contentBuilder.hearingLocation(questionnaire.getHearingLocation().getCourtName());
            contentBuilder.locationReason(questionnaire.getHearingLocation().getExceptionalCircumstancesReason().orElse(""));
            contentBuilder.hasExpertReport(questionnaire.getExpertReports().isEmpty() ? "NO" : "YES");

            questionnaire.getExpertRequest().ifPresent(req -> mapWitnessDetails.accept(req, contentBuilder));

            contentBuilder.expertReports(Optional.ofNullable(questionnaire.getExpertReports()).orElse(Collections.emptyList()));
            return contentBuilder.build();
        };

}
