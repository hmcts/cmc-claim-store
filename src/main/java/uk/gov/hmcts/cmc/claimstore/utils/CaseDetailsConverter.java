package uk.gov.hmcts.cmc.claimstore.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CaseDetailsConverter {

    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final int intentionToProceedDeadline;
    private final WorkingDayIndicator workingDayIndicator;

    public CaseDetailsConverter(
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        WorkingDayIndicator workingDayIndicator,
        @Value("${intention.to.proceed.deadline:33}") int intentionToProceedDeadline
    ) {

        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.intentionToProceedDeadline = intentionToProceedDeadline;
        this.workingDayIndicator = workingDayIndicator;
    }

    public Claim extractClaim(CaseDetails caseDetails) {
        Claim claim = caseMapper.from(extractCCDCase(caseDetails));

        if (claim.getRespondedAt() == null) {
            return claim;
        }

        // Calculating the intention to proceed here rather than in the mapper as we have access
        // to the WorkingDayIndicator here
        LocalDate intentionToProceedDeadline = calculateIntentionToProceedDeadline(claim.getRespondedAt());
        return claim.toBuilder()
                    .intentionToProceedDeadline(intentionToProceedDeadline)
                    .build();
    }

    public CCDCase extractCCDCase(CaseDetails caseDetails) {
        Map<String, Object> tempData = new HashMap<>(caseDetails.getData());
        tempData.put("id", caseDetails.getId());
        tempData.put("state", caseDetails.getState());

        return extractCCDCase(tempData);
    }

    private CCDCase extractCCDCase(Map<String, Object> mapData) {
        return jsonMapper.fromMap(mapData, CCDCase.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToMap(CCDCase ccdCase) {
        return (Map<String, Object>) jsonMapper.convertValue(ccdCase, Map.class);
    }

    private LocalDate calculateIntentionToProceedDeadline(LocalDateTime respondedDate) {
        LocalDate deadline = respondedDate.toLocalDate().plusDays(this.intentionToProceedDeadline);
        deadline = workingDayIndicator.getNextWorkingDay(deadline);

        return deadline;
    }
}
