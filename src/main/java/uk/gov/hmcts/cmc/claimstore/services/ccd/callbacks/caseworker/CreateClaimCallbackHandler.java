package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.constants.ResponseConstants.CREATE_CLAIM_DISABLED;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.SOLICITOR;

@Service
public class CreateClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CASE);
    private static final List<Role> ROLES = List.of(CITIZEN, SOLICITOR, CASEWORKER, LEGAL_ADVISOR, JUDGE);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;
    private final boolean featureCreateClaimEnabled;

    @Autowired
    public CreateClaimCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        @Value("${feature_toggles.create_claim_enabled:true}") boolean featureCreateClaimEnabled
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.featureCreateClaimEnabled = featureCreateClaimEnabled;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(CallbackType.ABOUT_TO_SUBMIT, this::createClaim);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    @LogExecutionTime
    private CallbackResponse createClaim(CallbackParams callbackParams) {
        logger.info("Create claim feature is: {}", featureCreateClaimEnabled ? "enabled" : "disabled");
        if (!featureCreateClaimEnabled) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List
                            .of(CREATE_CLAIM_DISABLED)).build();
        }
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Creating case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .build();
    }
}
