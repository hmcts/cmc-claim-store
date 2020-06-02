package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.IssuePaperDefenceForms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.ChangeContactDetailsPostProcessor;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class IssuePaperResponsePostProccessor {

    private static final String ERROR_MESSAGE =
            "There was a technical problem. Nothing has been sent. You need to try again.";

    private static final Logger logger = LoggerFactory.getLogger(ChangeContactDetailsPostProcessor.class);
    private final CaseDetailsConverter caseDetailsConverter;
    private final EventProducer eventProducer;
    private final IssuePaperResponseNotificationService issuePaperResponseNotificationService;

    @Autowired
    public IssuePaperResponsePostProccessor(
            CaseDetailsConverter caseDetailsConverter,
            EventProducer eventProducer,
            IssuePaperResponseNotificationService issuePaperResponseNotificationService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.eventProducer = eventProducer;
        this.issuePaperResponseNotificationService = issuePaperResponseNotificationService;
    }

    CallbackResponse performPostProcesses(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        LocalDate responseDeadline = ccdCase.getCalculatedResponseDeadline();
        LocalDate serviceDate = ccdCase.getCalculatedServiceDate();
        //claim updating here correct? -> should ccd case be updated at end?
        Claim updatedClaim = claim.toBuilder()
                .responseDeadline(responseDeadline)
                .serviceDate(serviceDate)
                .build();

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        try {
            issuePaperResponseNotificationService.notifyClaimant(updatedClaim);
            eventProducer.createPaperDefenceEvent(updatedClaim, ccdCase.getDraftLetterDoc(), ccdCase.getCoverLetterDoc());
        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
        return builder.data(caseDetailsConverter.convertToMap(ccdCase)).build();
    }
}
