package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ExpertReportContent {

    @NotNull
    private final String expertName;
    @NotNull
    private final String expertReportDate;
}
