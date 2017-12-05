package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.client.SaveCaseService;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class SaveClaimInCCDHandler {

    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String EVENT_ID = "submitClaimEvent";

    private final SaveCaseService saveCaseService;
    private final CaseMapper caseMapper;

    @Autowired
    public SaveClaimInCCDHandler(final SaveCaseService saveCaseService, final CaseMapper caseMapper) {
        this.saveCaseService = saveCaseService;
        this.caseMapper = caseMapper;
    }

    @EventListener
    public void saveClaimInCCD(RepresentedClaimIssuedEvent event) {
        final Claim claim = event.getClaim();
        final CCDCase ccdCase = caseMapper.to(claim);
        final EventRequestData eventRequestData = EventRequestData.builder()
            .userId(claim.getSubmitterId())
            .jurisdictionId(JURISDICTION_ID)
            .caseTypeId(CASE_TYPE_ID)
            .eventId(EVENT_ID)
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails = saveCaseService.save(event.getAuthorisation(), "", eventRequestData,
            ccdCase);
    }
}
