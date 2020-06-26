package uk.gov.hmcts.cmc.claimstore.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.util.MapperUtil;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseMethod;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
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
    private final boolean ctscEnabled;

    public CaseDetailsConverter(
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        WorkingDayIndicator workingDayIndicator,
        @Value("${intention.to.proceed.deadline:33}") int intentionToProceedDeadline,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled
    ) {

        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.intentionToProceedDeadline = intentionToProceedDeadline;
        this.workingDayIndicator = workingDayIndicator;
        this.ctscEnabled = ctscEnabled;
    }

    public Claim extractClaim(CaseDetails caseDetails) {
        CCDCase ccdCase = extractCCDCase(caseDetails);
        Claim claim = caseMapper.from(ccdCase);

        if (claim.getRespondedAt() == null) {
            return claim;
        }

        // Calculating the intention to proceed here rather than in the mapper as we have access
        // to the WorkingDayIndicator here
        LocalDate intentionToProceedDeadline = calculateIntentionToProceedDeadline(claim.getRespondedAt());
        return claim.toBuilder()
            .intentionToProceedDeadline(intentionToProceedDeadline)
            .response(updateResponseMethod(claim.getResponse().orElse(null), ccdCase))
            .build();
    }

    private Response updateResponseMethod(Response response, CCDCase ccdCase) {
        if (!ctscEnabled || response == null || response.getResponseMethod().isPresent()) {
            return response;
        }

        // Prior to ROC-7939 Response method was determined from documents attached to claim
        ResponseMethod responseMethod = MapperUtil.hasPaperResponse.apply(ccdCase) == YesNoOption.YES
            ? ResponseMethod.OFFLINE : ResponseMethod.DIGITAL;

        if (response instanceof PartAdmissionResponse) {
            return ((PartAdmissionResponse) response).toBuilder().responseMethod(responseMethod).build();
        } else if (response instanceof FullAdmissionResponse) {
            return ((FullAdmissionResponse) response).toBuilder().responseMethod(responseMethod).build();
        } else if (response instanceof FullDefenceResponse) {
            return ((FullDefenceResponse) response).toBuilder().responseMethod(responseMethod).build();
        }
        throw new IllegalArgumentException("Unhandled response type");
    }

    public CCDCase extractCCDCase(CaseDetails caseDetails) {
        Map<String, Object> tempData = new HashMap<>(caseDetails.getData());
        tempData.put("id", caseDetails.getId());
        tempData.put("state", caseDetails.getState());

        return extractCCDCase(tempData);
    }

    public CCDCase convertTo(Claim claim) {
        return caseMapper.to(claim);
    }

    private CCDCase extractCCDCase(Map<String, Object> mapData) {
        return jsonMapper.fromMap(mapData, CCDCase.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToMap(CCDCase ccdCase) {
        return (Map<String, Object>) jsonMapper.convertValue(ccdCase, Map.class);
    }

    public LocalDate calculateIntentionToProceedDeadline(LocalDateTime respondedDate) {
        LocalDate deadline = respondedDate.toLocalDate().plusDays(this.intentionToProceedDeadline);
        deadline = workingDayIndicator.getNextWorkingDay(deadline);

        return deadline;
    }
}
