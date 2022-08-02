package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;

import java.util.List;

@Builder
@Value
public class HearingContent {
    List<String> supportRequired;
    String hearingLocation;
    String locationReason;
    String hasExpertReport;
    List<ExpertReportContent> expertReports;
    String courtPermissionForExpertReport;
    String expertRequired;
    String expertExamineNeeded;
    String whatToExamine;
    String reasonWhyExpertAdvice;
    String evidenceRequired;
    Witness witness;
    List<String> unavailableDates;
}
