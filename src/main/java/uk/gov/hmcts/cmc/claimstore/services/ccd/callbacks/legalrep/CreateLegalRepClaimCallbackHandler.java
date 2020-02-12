package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legalrep;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_LEGAL_REP_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.SOLICITOR;
import static uk.gov.hmcts.cmc.domain.models.ChannelType.LEGAL_REP;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class CreateLegalRepClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_LEGAL_REP_CLAIM);
    private static final List<Role> ROLES = Collections.singletonList(SOLICITOR);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final IssueDateCalculator issueDateCalculator;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final CaseMapper caseMapper;

    @Autowired
    public CreateLegalRepClaimCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        IssueDateCalculator issueDateCalculator,
        ReferenceNumberRepository referenceNumberRepository,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        CaseMapper caseMapper
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.issueDateCalculator = issueDateCalculator;
        this.referenceNumberRepository = referenceNumberRepository;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.caseMapper = caseMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(CallbackType.ABOUT_TO_SUBMIT, this::createLegalRepClaim);
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
    private CallbackResponse createLegalRepClaim(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Creating legal rep case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        String referenceNumber = referenceNumberRepository.getReferenceNumberForLegal();

        Claim updatedClaim = claim.toBuilder()
            .referenceNumber(referenceNumber)
            .issuedOn(issuedOn)
            .serviceDate(issuedOn.plusDays(5))
            .responseDeadline(responseDeadline)
            .channel(LEGAL_REP)
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(updatedClaim)))
            .build();
    }
}
