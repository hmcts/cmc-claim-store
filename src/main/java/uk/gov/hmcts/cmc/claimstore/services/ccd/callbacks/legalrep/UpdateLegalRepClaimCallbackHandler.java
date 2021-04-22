package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legalrep;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
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
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_LEGAL_REP_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.SOLICITOR;
import static uk.gov.hmcts.cmc.domain.models.ChannelType.LEGAL_REP;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class UpdateLegalRepClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Arrays.asList(UPDATE_LEGAL_REP_CLAIM);
    private static final List<Role> ROLES = Collections.singletonList(SOLICITOR);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final IssueDateCalculator issueDateCalculator;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final CaseMapper caseMapper;

    @Autowired
    public UpdateLegalRepClaimCallbackHandler(
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
        return ImmutableMap.of(CallbackType.ABOUT_TO_SUBMIT, this::updateLegalRepClaim);
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
    private CallbackResponse updateLegalRepClaim(CallbackParams callbackParams) {
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Updating legal rep case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());
        if (PaymentStatus.fromValue(ccdCase.getPaymentStatus()).equals(PaymentStatus.SUCCESS)) {
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
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .build();
    }
}
