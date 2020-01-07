package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.events.claim.GeneratedDocuments;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.response.DefendantLinkStatus;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

public class ResendNewPinCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.RESEND_PIN);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CaseDetailsConverter caseDetailsConverter;

    private UserService userService;

    private CitizenServiceDocumentsService citizenServiceDocumentsService;

    private PDFServiceClient pdfServiceClient;

    private CaseMapper caseMapper;

    private EventProducer eventProducer;

    private ClaimIssuedNotificationService claimIssuedNotificationService;

    private NotificationsProperties notificationsProperties;


    @Autowired
    public ResendNewPinCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        UserService userService,
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        PDFServiceClient pdfServiceClient,
        CaseMapper caseMapper,
        EventProducer eventProducer,
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.userService = userService;
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.pdfServiceClient = pdfServiceClient;
        this.caseMapper = caseMapper;
        this.eventProducer = eventProducer;
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::resendNewPin
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse resendNewPin(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        if (getDefendantLinkStatus(claim).isLinked()) {
            logger.info("Claim {} has already been linked to defendant - cannot send notification", claim.getExternalId());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of("Claim has already been linked to defendant - cannot send notification"))
                .build();
        }

        Optional<GeneratePinResponse> pinResponse = getPinResponse(claim.getClaimData(), authorisation);

        String pin = pinResponse
            .map(GeneratePinResponse::getPin)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        String letterHolderId = pinResponse
            .map(GeneratePinResponse::getUserId)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        claim.getClaimData().getDefendant().getEmail().ifPresent(defendantEmail ->
            claimIssuedNotificationService.sendMail(
                claim,
                defendantEmail,
                pin,
                notificationsProperties.getTemplates().getEmail().getDefendantClaimIssued(),
                "defendant-issue-notification-" + claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()
            ));

        Claim updatedClaim = claim.toBuilder()
            .letterHolderId(letterHolderId)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(updatedClaim)))
            .build();
    }

    private Optional<GeneratePinResponse> getPinResponse(ClaimData claimData, String authorisation) {
        return Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
    }

    private DefendantLinkStatus getDefendantLinkStatus(Claim claim) {
        if (claim.getDefendantId() != null) {
            return new DefendantLinkStatus(false);
        }
        return new DefendantLinkStatus(true);
    }
}
