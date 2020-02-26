package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDResponseSubjectType;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackVersion;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.GenerateOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.DOCUMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EYEWITNESS;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class OrderCreator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DOC_UPLOAD_DEADLINE = "docUploadDeadline";
    private static final String EYEWITNESS_UPLOAD_DEADLINE = "eyewitnessUploadDeadline";
    private static final String DOC_UPLOAD_FOR_PARTY = "docUploadForParty";
    private static final String EYEWITNESS_UPLOAD_FOR_PARTY = "eyewitnessUploadForParty";
    private static final String PAPER_DETERMINATION = "paperDetermination";
    private static final String DRAFT_ORDER_DOC = "draftOrderDoc";
    private static final String NEW_REQUESTED_COURT = "newRequestedCourt";
    private static final String PREFERRED_COURT_OBJECTING_PARTY = "preferredCourtObjectingParty";
    private static final String PREFERRED_COURT_OBJECTING_REASON = "preferredCourtObjectingReason";
    private static final String DIRECTION_LIST = "directionList";
    private static final String PREFERRED_DQ_COURT = "preferredDQCourt";
    private static final String EXPERT_PERMISSION_BY_CLAIMANT = "expertReportPermissionPartyAskedByClaimant";
    private static final String EXPERT_PERMISSION_BY_DEFENDANT = "expertReportPermissionPartyAskedByDefendant";
    private static final String HEARING_COURT = "hearingCourt";
    private static final String DYNAMIC_LIST_CODE = "code";
    private static final String DYNAMIC_LIST_LABEL = "label";
    private static final String DYNAMIC_LIST_ITEMS = "list_items";
    private static final String DYNAMIC_LIST_SELECTED_VALUE = "value";
    private static final String GRANT_EXPERT_REPORT_PERMISSION = "grantExpertReportPermission";
    private static final String EXPERT_REPORT_INSTRUCTION = "expertReportInstruction";
    private static final String OTHER_DIRECTIONS = "otherDirections";
    private static final String ESTIMATED_HEARING_DURATION = "estimatedHearingDuration";

    private final LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final GenerateOrderRule generateOrderRule;
    private final DirectionsQuestionnaireService directionsQuestionnaireService;
    private final PilotCourtService pilotCourtService;

    public OrderCreator(
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        GenerateOrderRule generateOrderRule,
        DirectionsQuestionnaireService directionsQuestionnaireService,
        PilotCourtService pilotCourtService
    ) {
        this.legalOrderGenerationDeadlinesCalculator = legalOrderGenerationDeadlinesCalculator;
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.generateOrderRule = generateOrderRule;
        this.directionsQuestionnaireService = directionsQuestionnaireService;
        this.pilotCourtService = pilotCourtService;
    }

    public CallbackResponse prepopulateOrder(CallbackParams callbackParams) {
        logger.info("Order creator: pre populating order fields");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        Map<String, Object> data = new HashMap<>();
        data.put(DIRECTION_LIST, chooseItem(ccdCase.getDirectionList(), ImmutableList.of(DOCUMENTS, EYEWITNESS)));

        addCourtData(claim, ccdCase, data);

        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        data.put(DOC_UPLOAD_DEADLINE, chooseItem(ccdCase.getDocUploadDeadline(), deadline));
        data.put(EYEWITNESS_UPLOAD_DEADLINE, chooseItem(ccdCase.getEyewitnessUploadDeadline(), deadline));
        data.put(DOC_UPLOAD_FOR_PARTY, chooseItem(ccdCase.getDocUploadForParty(), BOTH));
        data.put(EYEWITNESS_UPLOAD_FOR_PARTY, chooseItem(ccdCase.getEyewitnessUploadForParty(), BOTH));
        data.put(PAPER_DETERMINATION, chooseItem(ccdCase.getPaperDetermination(), NO));
        data.put(ESTIMATED_HEARING_DURATION, ccdCase.getEstimatedHearingDuration());

        data.put(OTHER_DIRECTIONS, ccdCase.getOtherDirections());

        if (hasDynamicCourts(callbackParams)) {
            data.put(GRANT_EXPERT_REPORT_PERMISSION, chooseItem(ccdCase.getGrantExpertReportPermission(), NO));
            data.put(EXPERT_REPORT_INSTRUCTION, chooseItem(ccdCase.getExpertReportInstruction(), null));
        }

        if (hasExpertsAtCaseLevel(callbackParams)) {
            data.put(HEARING_COURT,
                buildCourtsList(getPilot(callbackParams), claim.getCreatedAt(), ccdCase.getHearingCourtName()));

            if (ccdCase.getGrantExpertReportPermission() == null
                && (ccdCase.getExpertReportPermissionPartyGivenToClaimant() != null
                || ccdCase.getExpertReportPermissionPartyGivenToDefendant() != null)) {

                data.put(GRANT_EXPERT_REPORT_PERMISSION, hasExpertPermission(ccdCase) ? YES : NO);

                String instructions = Stream.of(extractInstructions(ccdCase.getExpertReportInstructionClaimant()),
                    extractInstructions(ccdCase.getExpertReportInstructionDefendant()))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(", "));

                data.put(EXPERT_REPORT_INSTRUCTION, instructions);

            }
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(data)
            .build();
    }

    private static <T> T chooseItem(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private static <T> List<T> chooseItem(List<T> list, List<T> defaultList) {
        return list != null && !list.isEmpty() ? list : defaultList;
    }

    private boolean hasExpertPermission(CCDCase ccdCase) {
        return ccdCase.getExpertReportPermissionPartyGivenToClaimant().toBoolean()
            || ccdCase.getExpertReportPermissionPartyGivenToDefendant().toBoolean();
    }

    private String extractInstructions(List<CCDCollectionElement<String>> expertReportInstructions) {
        return expertReportInstructions.stream()
            .map(CCDCollectionElement::getValue)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(", "));
    }

    public CallbackResponse generateOrder(CallbackParams callbackParams) {
        logger.info("Order creator: creating order document");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        List<String> validations = generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase,
            hasExpertsAtCaseLevel(callbackParams));
        if (!validations.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validations).build();
        }

        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createOrder(ccdCase, authorisation);
        logger.info("Order creator: received response from doc assembly");

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                DRAFT_ORDER_DOC,
                CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
            ))
            .build();
    }

    private void addCourtData(Claim claim, CCDCase ccdCase, Map<String, Object> data) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();

        CCDResponseRejection claimantResponse = Optional.ofNullable(respondent.getClaimantResponse())
            .map(CCDResponseRejection.class::cast)
            .orElseThrow(() -> new IllegalStateException("Claimant Response not present"));

        CCDDirectionsQuestionnaire claimantDQ = claimantResponse.getDirectionsQuestionnaire();
        CCDDirectionsQuestionnaire defendantDQ = respondent.getDirectionsQuestionnaire();

        String newRequestedCourt = null;
        String preferredCourtObjectingParty = null;
        String preferredCourtObjectingReason = null;

        if (claimantDQ != null && StringUtils.isNotBlank(claimantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = claimantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_CLAIMANT.getValue();
            preferredCourtObjectingReason = claimantDQ.getExceptionalCircumstancesReason();

        } else if (defendantDQ != null && StringUtils.isNotBlank(defendantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = defendantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_DEFENDANT.getValue();
            preferredCourtObjectingReason = defendantDQ.getExceptionalCircumstancesReason();
        }

        data.put(NEW_REQUESTED_COURT, newRequestedCourt);
        data.put(PREFERRED_COURT_OBJECTING_PARTY, preferredCourtObjectingParty);
        data.put(PREFERRED_COURT_OBJECTING_REASON, preferredCourtObjectingReason);
        data.put(PREFERRED_DQ_COURT, directionsQuestionnaireService.getPreferredCourt(claim));

        if (Optional.ofNullable(claimantDQ).isPresent()) {
            data.put(EXPERT_PERMISSION_BY_CLAIMANT, hasRequestedExpertPermission(claimantDQ));
        }

        if (Optional.ofNullable(defendantDQ).isPresent()) {
            data.put(EXPERT_PERMISSION_BY_DEFENDANT, hasRequestedExpertPermission(defendantDQ));
        }
    }

    private CCDYesNoOption hasRequestedExpertPermission(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getExpertRequired() != null
            && directionsQuestionnaire.getExpertRequired().toBoolean()
            && (hasRequestedForPermissionWithProvidedEvidence(directionsQuestionnaire)
            || hasProvidedExpertReports(directionsQuestionnaire))
            ? YES
            : NO;
    }

    private boolean hasProvidedExpertReports(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getExpertReports() != null
            && !directionsQuestionnaire.getExpertReports().isEmpty();
    }

    private boolean hasRequestedForPermissionWithProvidedEvidence(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getPermissionForExpert() != null
            && directionsQuestionnaire.getPermissionForExpert().toBoolean()
            && StringUtils.isNotBlank(directionsQuestionnaire.getExpertEvidenceToExamine());
    }

    private Map<String, Object> buildCourtsList(Pilot pilot, LocalDateTime claimCreatedDate, String hearingCourtName) {
        List<Map<String, String>> listItems = pilotCourtService.getPilotHearingCourts(pilot, claimCreatedDate).stream()
            .sorted(Comparator.comparing(HearingCourt::getName))
            .map(hearingCourt -> {
                String id =  pilotCourtService.getPilotCourtId(hearingCourt);
                return ImmutableMap.of(DYNAMIC_LIST_CODE, id, DYNAMIC_LIST_LABEL, hearingCourt.getName());
            })
            .collect(Collectors.toList());

        ImmutableMap<String, String> otherCourtItem = ImmutableMap.of(DYNAMIC_LIST_CODE,
            PilotCourtService.OTHER_COURT_ID, DYNAMIC_LIST_LABEL, "Other Court");

        if (pilot == Pilot.JDDO) {
            listItems.add(otherCourtItem);
        }

        Map<String, Object> hearingCourtListDefinition = new HashMap<>();
        hearingCourtListDefinition.put(DYNAMIC_LIST_ITEMS, listItems);

        if (StringUtils.isBlank(hearingCourtName)) {
            return hearingCourtListDefinition;
        }

        Optional<Map<String, String>> selectedCourt =
            listItems.stream().filter(s -> s.get(DYNAMIC_LIST_LABEL).equals(hearingCourtName)).findFirst();

        if (selectedCourt.isPresent()) {
            hearingCourtListDefinition.put(DYNAMIC_LIST_SELECTED_VALUE, selectedCourt.get());
        } else if (pilot == Pilot.JDDO) {
            hearingCourtListDefinition.put(DYNAMIC_LIST_SELECTED_VALUE, otherCourtItem);
        }

        return hearingCourtListDefinition;
    }

    private Pilot getPilot(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.fromValue(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case GENERATE_ORDER:
            case ACTION_REVIEW_COMMENTS:
                return Pilot.LA;
            case DRAW_JUDGES_ORDER:
                return Pilot.JDDO;
            default:
                throw new IllegalArgumentException("No pilot defined for event: " + caseEvent);
        }
    }

    private boolean hasExpertsAtCaseLevel(CallbackParams callbackParams) {
        return getPilot(callbackParams) != Pilot.LA || callbackParams.getVersion() == CallbackVersion.V_2;
    }

    private boolean hasDynamicCourts(CallbackParams callbackParams) {
        return getPilot(callbackParams) != Pilot.LA || callbackParams.getVersion() == CallbackVersion.V_2;
    }
}
