package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_FULL_DEFENCE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseOCON9xFormCallbackHandler.OCON9X_SUBTYPE;

@Service
public class PaperResponseFullDefenceCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(PAPER_RESPONSE_FULL_DEFENCE);
    private static final String OCON9X_REVIEW =
        "Before continuing you must complete the ‘Review OCON9x paper response’ event";
    private final CaseDetailsConverter caseDetailsConverter;
    private final Clock clock;
    private final EventProducer eventProducer;
    private final CaseMapper caseMapper;
    private final CourtFinderApi courtFinderApi;
    private final UserService userService;
    private final CaseEventService caseEventService;
    private final LaunchDarklyClient launchDarklyClient;
    private final DefenceResponseNotificationService defenceResponseNotificationService;

    public PaperResponseFullDefenceCallbackHandler(CaseDetailsConverter caseDetailsConverter, Clock clock,
                                                   EventProducer eventProducer, CaseMapper caseMapper,
                                                   CourtFinderApi courtFinderApi,
                                                   UserService userService,
                                                   CaseEventService caseEventService,
                                                   LaunchDarklyClient launchDarklyClient,
                                                   DefenceResponseNotificationService
                                                       defenceResponseNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.clock = clock;
        this.eventProducer = eventProducer;
        this.caseMapper = caseMapper;
        this.courtFinderApi = courtFinderApi;
        this.userService = userService;
        this.caseEventService = caseEventService;
        this.launchDarklyClient = launchDarklyClient;
        this.defenceResponseNotificationService = defenceResponseNotificationService;
    }

    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::aboutToStart,
            CallbackType.ABOUT_TO_SUBMIT, this::aboutToSubmit,
            CallbackType.SUBMITTED, this::submitted
        );
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (launchDarklyClient.isFeatureEnabled("ocon-enhancements", LaunchDarklyClient.CLAIM_STORE_USER)) {
            List<CaseEvent> caseEventList = caseEventService.findEventsForCase(
                String.valueOf(ccdCase.getId()), userService.getUser(authorisation));
            boolean eventPresent = caseEventList.stream()
                .anyMatch(caseEvent -> caseEvent.getValue().equals("PaperResponseOCON9xForm"));
            if (!eventPresent) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(OCON9X_REVIEW)).build();
            }
        }

        Map<String, Object> data = new HashMap<>(caseDetailsConverter.convertToMap(ccdCase));

        if (FeaturesUtils.isOnlineDQ(caseDetailsConverter.extractClaim(caseDetails))
            && StringUtils.isBlank(ccdCase.getPreferredCourt())) {

            CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
            CCDPartyType partyType = ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail().getType();
            CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
            CCDParty givenRespondent = respondent.getClaimantProvidedDetail();

            CCDAddress defendantAddress = getDefendantAddress(respondent, givenRespondent);
            CCDAddress claimantAddress = getClaimantAddress(applicant);
            String courtName = getCourtName(partyType, defendantAddress, claimantAddress);

            data.put("preferredDQCourt", courtName);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

        List<CCDCollectionElement<CCDRespondent>> updatedRespondents = updateRespondents(caseDetails, ccdCase);

        List<CCDCollectionElement<CCDScannedDocument>> updatedScannedDocuments = updateScannedDocuments(ccdCase);

        LocalDate intentionToProceedDeadline =
            caseDetailsConverter.calculateIntentionToProceedDeadline(LocalDateTime.now(clock));

        CCDCase updatedCcdCase = ccdCase.toBuilder()
            .respondents(updatedRespondents)
            .scannedDocuments(updatedScannedDocuments)
            .intentionToProceedDeadline(intentionToProceedDeadline)
            .build();

        eventProducer.createDefendantPaperResponseEvent(caseMapper.from(updatedCcdCase), authorisation);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCcdCase))
            .build();
    }

    private CallbackResponse submitted(CallbackParams callbackParams) {
        Party defendant = null;
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails())
            .toBuilder().lastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId()).build();
        Optional<Response> response = claim.getResponse();
        if (response.isPresent()) {
            defendant = response.get().getDefendant();
        }
        if (defendant != null && defendant.getAddress() == null) {
            Party updatedParty = null;
            if (defendant.getClass().equals(Individual.class)) {
                Individual individualDefendant = (Individual) defendant);
                updatedParty = individualDefendant.builder()
                    .address(claim.getClaimData().getDefendant().getAddress()).build();
            } else if (defendant.getClass().equals(Company.class)) {
                updatedParty = ((Company) defendant).builder()
                    .address(claim.getClaimData().getDefendant().getAddress()).build();
            } else if (defendant.getClass().equals(Organisation.class)) {
                updatedParty = ((Organisation) defendant).builder()
                    .address(claim.getClaimData().getDefendant().getAddress()).build();
            } else if (defendant.getClass().equals(SoleTrader.class)) {
                updatedParty = ((SoleTrader) defendant).builder()
                    .address(claim.getClaimData().getDefendant().getAddress()).build();
            }
            Response updatedResponse = ((FullDefenceResponse) response.get())
                .toBuilder().defendant(updatedParty).build();
            claim = claim.toBuilder().response(updatedResponse).build();
        }
        DefendantResponseEvent event = new DefendantResponseEvent(claim, authorisation);
        defenceResponseNotificationService.notifyRobotics(event);
        return SubmittedCallbackResponse.builder().build();
    }

    private List<CCDCollectionElement<CCDScannedDocument>> updateScannedDocuments(CCDCase ccdCase) {
        return ccdCase.getScannedDocuments()
            .stream()
            .map(e -> OCON9X_SUBTYPE.equals(e.getValue().getSubtype()) ? updateFilename(e, ccdCase) : e)
            .collect(Collectors.toList());
    }

    private List<CCDCollectionElement<CCDRespondent>> updateRespondents(CaseDetails caseDetails, CCDCase ccdCase) {

        LocalDateTime respondedDate = getResponseDate(ccdCase);

        return ccdCase.getRespondents()
            .stream()
            .map(r -> r.toBuilder()
                .value(r.getValue()
                    .toBuilder()
                    .responseType(CCDResponseType.FULL_DEFENCE)
                    .responseDefenceType(CCDDefenceType.valueOf(getCaseDetailsProperty(caseDetails, "defenceType")))
                    .partyDetail(getPartyDetail(r)
                        .toBuilder()
                        .emailAddress(getEmailAddress(r))
                        .type(r.getValue().getClaimantProvidedDetail().getType())
                        .build())
                    .responseSubmittedOn(respondedDate)
                    .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder()
                        .hearingLocation(getCaseDetailsProperty(caseDetails, "preferredDQCourt"))
                        .build())
                    .build())
                .build())
            .collect(Collectors.toList());
    }

    private CCDParty getPartyDetail(CCDCollectionElement<CCDRespondent> e) {
        return e.getValue().getPartyDetail() != null ? e.getValue().getPartyDetail() :
            e.getValue().getClaimantProvidedDetail();
    }

    private String getCaseDetailsProperty(CaseDetails caseDetails, String preferredDQCourt) {
        return (String) caseDetails.getData().get(preferredDQCourt);
    }

    private String getEmailAddress(CCDCollectionElement<CCDRespondent> r) {
        var partyDetail = r.getValue().getPartyDetail();
        return partyDetail != null && !StringUtils.isBlank(partyDetail.getEmailAddress())
            ? partyDetail.getEmailAddress()
            : r.getValue().getClaimantProvidedDetail().getEmailAddress();
    }

    private LocalDateTime getResponseDate(CCDCase ccdCase) {
        return ccdCase.getScannedDocuments()
            .stream()
            .filter(e -> OCON9X_SUBTYPE.equals(e.getValue().getSubtype()))
            .map(CCDCollectionElement::getValue)
            .map(CCDScannedDocument::getDeliveryDate)
            .max(LocalDateTime::compareTo)
            .orElseThrow(() -> new IllegalStateException("No OCON9x form found"));
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

    private String getCourtName(CCDPartyType partyType, CCDAddress defendantAddress, CCDAddress claimantAddress) {
        return courtFinderApi.findMoneyClaimCourtByPostcode((partyType == CCDPartyType.COMPANY
            || partyType == CCDPartyType.ORGANISATION)
            ? claimantAddress.getPostCode() : defendantAddress.getPostCode())
            .stream()
            .map(Court::getName)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No court found"));
    }

    private CCDAddress getClaimantAddress(CCDApplicant applicant) {
        return applicant.getPartyDetail().getPrimaryAddress();
    }

    private CCDAddress getDefendantAddress(CCDRespondent respondent, CCDParty givenRespondent) {

        return respondent.getPartyDetail() != null && respondent.getPartyDetail().getPrimaryAddress() != null
            ? respondent.getPartyDetail().getPrimaryAddress() : givenRespondent.getPrimaryAddress();
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
