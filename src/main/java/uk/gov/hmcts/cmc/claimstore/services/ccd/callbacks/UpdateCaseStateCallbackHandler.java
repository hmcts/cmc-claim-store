package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.staff.SaveClaimantResponseDocumentService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
public class UpdateCaseStateCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Arrays.asList(CASEWORKER, JUDGE, LEGAL_ADVISOR);
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;
    private final SaveClaimantResponseDocumentService saveClaimantResponseDocumentService;
    private static final String CANNOT_UPDATE = "cannot update the case for the given claim";

    @Autowired
    public UpdateCaseStateCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        SaveClaimantResponseDocumentService saveClaimantResponseDocumentService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.saveClaimantResponseDocumentService = saveClaimantResponseDocumentService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::changeStateToProceedToCaseMan
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.PROCEEDS_IN_CASEMAN);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse changeStateToProceedToCaseMan(CallbackParams callbackParams) {
        Map<String, Object> dataMap = new HashMap<>();
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        if (claim.getCountyCourtJudgment() != null
            && (claim.getCountyCourtJudgment().getCcjType().equals(CountyCourtJudgmentType.ADMISSIONS)
            || claim.getCountyCourtJudgment().getCcjType().equals(CountyCourtJudgmentType.DETERMINATION))) {
            saveClaimantResponseDocumentService.getAndSaveDocumentToCcd(claim);
            CCDCase ccdCase = caseMapper.to(claim);
            ccdCase.setState(ClaimState.PROCEEDS_IN_CASE_MAN.getValue());
            dataMap = caseDetailsConverter.convertToMap(ccdCase);

        } else {
            dataMap.put(claim.getReferenceNumber(), CANNOT_UPDATE);
        }
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(dataMap)
            .build();
    }
}
