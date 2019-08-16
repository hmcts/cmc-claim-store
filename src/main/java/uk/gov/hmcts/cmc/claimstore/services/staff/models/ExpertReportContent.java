package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Builder
@Value
public class ExpertReportContent {

    @NotNull
    private final String expertName;
    @NotNull
    private final String expertReportDate;
}
