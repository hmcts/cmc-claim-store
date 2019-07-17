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
public class HearingContentProvider {

    public final String DISABLED_ACCESS = "Disabled Access";

    private BiConsumer<RequireSupport, HearingContent.HearingContentBuilder> mapSupportRequirement =
        (support, builder) -> {
            List<String> supportNeeded = new ArrayList<>();
            support.getLanguageInterpreter().ifPresent(supportNeeded::add);
            support.getSignLanguageInterpreter().ifPresent(supportNeeded::add);
            support.getOtherSupport().ifPresent(supportNeeded::add);
            support.getDisabledAccess()
                .map(YesNoOption::name)
                .filter(access -> access.equals(YesNoOption.YES.name()))
                .ifPresent(x -> supportNeeded.add(DISABLED_ACCESS));
            builder.supportRequired(supportNeeded);
        };

    private BiConsumer<ExpertRequest, HearingContent.HearingContentBuilder> mapExpertRequest =
        (expertRequest, builder) -> {
            builder.hasExpertReport("YES");
            builder.courtPermissionForExpertReport("YES");
            builder.reasonWhyExpertAdvice(expertRequest.getReasonForExpertAdvice());
            builder.expertExamineNeeded(expertRequest.getExpertEvidenceToExamine());
        };

    public Function<DirectionsQuestionnaire, HearingContent> mapDirectionQuestionnaire =
        questionnaire -> {

            Optional.ofNullable(questionnaire).orElseThrow(IllegalArgumentException::new);

            HearingContent.HearingContentBuilder contentBuilder = HearingContent.builder();

            questionnaire.getRequireSupport()
                .ifPresent(support -> mapSupportRequirement.accept(support, contentBuilder));

            contentBuilder.hearingLocation(questionnaire.getHearingLocation().getCourtName());
            contentBuilder.locationReason(questionnaire.getHearingLocation().getExceptionalCircumstancesReason()
                .orElse(""));
            contentBuilder.hasExpertReport(questionnaire.getExpertReports().isEmpty() ? "NO" : "YES");

            questionnaire.getWitness().ifPresent(contentBuilder::witness);
            questionnaire.getExpertRequest().ifPresent(req -> mapExpertRequest.accept(req, contentBuilder));
            contentBuilder.unavailableDates(questionnaire.getUnavailableDates());

            contentBuilder.expertReports(Optional.ofNullable(questionnaire.getExpertReports()).orElse(Collections.emptyList()));
            return contentBuilder.build();
        };

}
