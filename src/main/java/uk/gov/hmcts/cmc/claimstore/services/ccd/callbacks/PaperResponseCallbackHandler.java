package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Component
public class PaperResponseCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private final CaseDetailsConverter caseDetailsConverter;

    private static Predicate<ClaimDocument> filterN19Doc = doc ->
        doc.getDocumentType().name().equalsIgnoreCase("");

    private static YesNoOption canContinueOnline(Claim claim){
        return claim.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .map(s -> s.stream().filter(filterN19Doc).findFirst())
            .isPresent()? YesNoOption.NO: YesNoOption.YES;
    }

    @Autowired
    public PaperResponseCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::determinePaperResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.REVIEWED_PAPER_RESPONSE);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse determinePaperResponse(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();

        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        CCDRespondent respondent = ccdCase.getRespondents().get(1).getValue();

        Response response = Response.builder().paperResponse()


        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }


}
