package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"feature_toggles.bulk_print_transfer_enabled"})
public class TransferCaseCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(TRANSFER);
    private final TransferCasePostProcessor transferCasePostProcessor;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final String HEARING_COURT = "hearingCourt";
    private static final String DYNAMIC_LIST_CODE = "code";
    private static final String DYNAMIC_LIST_LABEL = "label";
    private static final String DYNAMIC_LIST_ITEMS = "list_items";
    private static final String DYNAMIC_LIST_SELECTED_VALUE = "value";
    private final PilotCourtService pilotCourtService;

    @Autowired
    public TransferCaseCallbackHandler(
        TransferCasePostProcessor transferCasePostProcessor,
        CaseDetailsConverter caseDetailsConverter,
        PilotCourtService pilotCourtService
    ) {
        this.transferCasePostProcessor = transferCasePostProcessor;
        this.caseDetailsConverter = caseDetailsConverter;
        this.pilotCourtService = pilotCourtService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::prepopulateData,
            CallbackType.MID, transferCasePostProcessor::processCourt,
            CallbackType.ABOUT_TO_SUBMIT, transferCasePostProcessor::transferToCourt
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

    private CallbackResponse prepopulateData(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        var response = AboutToStartOrSubmitCallbackResponse.builder();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        CCDDirectionOrder directionOrder = ccdCase.getDirectionOrder();
        Map<String, Object> data = new HashMap<>();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        Map<String, Object> courtList = buildCourtsList(Pilot.CASEWORKER, claim.getCreatedAt(),
            ccdCase.getHearingCourtName());
        data.put(HEARING_COURT, courtList);

        if (directionOrder != null) {
            return response
                .data(Map.of("transferContent", CCDTransferContent.builder()
                    .transferCourtName(directionOrder.getHearingCourtName())
                    .transferCourtAddress(directionOrder.getHearingCourtAddress())
                    .build()))
                .build();
        }
        return response.data(data).build();
    }

    private Map<String, Object> buildCourtsList(Pilot pilot, LocalDateTime claimCreatedDate, String hearingCourtName) {
        List<Map<String, String>> listItems = pilotCourtService.getPilotHearingCourts(pilot, claimCreatedDate).stream()
            .sorted(Comparator.comparing(HearingCourt::getName))
            .map(hearingCourt -> {
                String id = pilotCourtService.getPilotCourtId(hearingCourt);
                return ImmutableMap.of(DYNAMIC_LIST_CODE, id, DYNAMIC_LIST_LABEL, hearingCourt.getName());
            })
            .collect(Collectors.toList());

        Map<String, Object> hearingCourtListDefinition = new HashMap<>();
        hearingCourtListDefinition.put(DYNAMIC_LIST_ITEMS, listItems);

        if (StringUtils.isBlank(hearingCourtName)) {
            return hearingCourtListDefinition;
        }

        Optional<Map<String, String>> selectedCourt =
            listItems.stream().filter(s -> s.get(DYNAMIC_LIST_LABEL).equals(hearingCourtName)).findFirst();

        selectedCourt.ifPresent(val -> hearingCourtListDefinition.put(DYNAMIC_LIST_SELECTED_VALUE, val));

        return hearingCourtListDefinition;
    }

}
