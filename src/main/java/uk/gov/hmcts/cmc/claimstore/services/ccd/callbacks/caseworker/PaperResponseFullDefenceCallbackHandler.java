package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_FULL_DEFENCE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class PaperResponseFullDefenceCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(PAPER_RESPONSE_FULL_DEFENCE);

    private final CaseDetailsConverter caseDetailsConverter;
    private final Clock clock;

    public PaperResponseFullDefenceCallbackHandler(CaseDetailsConverter caseDetailsConverter, Clock clock) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.clock = clock;
    }

    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_SUBMIT, this::aboutToSubmit
        );
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);

        var updatedRespondents = ccdCase.getRespondents()
            .stream()
            .map(r -> r.toBuilder()
                .value(r.getValue()
                    .toBuilder()
                    .responseType(CCDResponseType.FULL_DEFENCE)
                    .responseDefenceType(CCDDefenceType.valueOf((String)caseDetails.getData().get("defenceType")))
                    .partyDetail(r.getValue()
                        .getPartyDetail()
                        .toBuilder()
                        .type(r.getValue()
                            .getClaimantProvidedDetail()
                            .getType()).build()
                    )
                    .build())
                .build())
            .collect(Collectors.toList());

        var updatedScannedDocuments = ccdCase.getScannedDocuments()
            .stream()
            .map(e -> e.getValue().getSubtype().equals("OCON9x") ? updateFilename(e, ccdCase) : e)
            .collect(Collectors.toList());

        LocalDate intentionToProceedDeadline =
            caseDetailsConverter.calculateIntentionToProceedDeadline(LocalDateTime.now(clock));

        CCDCase updatedCcdCase = ccdCase.toBuilder()
            .respondents(updatedRespondents)
            .scannedDocuments(updatedScannedDocuments)
            .intentionToProceedDeadline(intentionToProceedDeadline)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCcdCase))
            .build();
    }

    private CCDCollectionElement<CCDScannedDocument> updateFilename(CCDCollectionElement<CCDScannedDocument> element,
                                                                    CCDCase ccdCase) {
        return element.toBuilder()
            .value(element.getValue()
                .toBuilder()
                .fileName(String.format("%s-scanned-OCON9x-full-defence.pdf",
                    ccdCase.getPreviousServiceCaseReference()))
                .build())
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }
}
