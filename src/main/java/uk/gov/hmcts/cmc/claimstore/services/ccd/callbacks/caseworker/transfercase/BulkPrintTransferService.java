package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BulkPrintTransferService {

    private CaseSearchApi caseSearchApi;

    private UserService userService;

    private CaseMapper caseMapper;
    private final TransferCaseDocumentPublishService transferCaseDocumentPublishService;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final CoreCaseDataService coreCaseDataService;
    private static final String REASON = "For directions";

    @Autowired
    public BulkPrintTransferService(
        CaseSearchApi caseSearchApi,
        UserService userService,
        CaseMapper caseMapper,
        TransferCaseDocumentPublishService transferCaseDocumentPublishService,
        TransferCaseNotificationsService transferCaseNotificationsService,
        CoreCaseDataService coreCaseDataService
    ) {
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.transferCaseDocumentPublishService = transferCaseDocumentPublishService;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void findCasesAndTransfer() {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        List<Claim> claimsReadyForTransfer = caseSearchApi.getClaimsReadyForTransfer(user);
        Map<Claim, CCDCase> ccdCaseMap = new HashMap<>();
        claimsReadyForTransfer.forEach(claim -> ccdCaseMap.put(claim, caseMapper.to(claim)));
        ccdCaseMap.forEach((claim, ccdCase) -> {
            CCDCase ccdCaseTransferred = transferCase(ccdCase, claim, authorisation);
            CCDCase ccdCaseUpdated =  updateTransferContent(ccdCaseTransferred);
            updateCaseInCCD(ccdCaseUpdated, authorisation);
        });
    }

    public CCDCase transferCase(CCDCase ccdCase, Claim claim, String authorisation) {
        CCDCase updated = transferCaseDocumentPublishService.publishCaseDocuments(ccdCase, authorisation, claim);
        sendEmailNotifications(updated, claim);
        return updateCaseData(updated);
    }

    private CCDCase updateTransferContent(CCDCase ccdCase) {
        return ccdCase.toBuilder()
            .transferContent(ccdCase.getTransferContent().toBuilder()
                .transferCourtName(ccdCase.getHearingCourtName())
                .transferCourtAddress(ccdCase.getHearingCourtAddress())
                .transferReason(CCDTransferReason.OTHER)
                .transferReasonOther(REASON).build())
            .build();
    }

    private CCDCase updateCaseData(CCDCase ccdCase) {

        CCDTransferContent transferContent;
        if (ccdCase.getTransferContent() != null) {
            transferContent = ccdCase.getTransferContent().toBuilder()
                .dateOfTransfer(LocalDate.now()).build();
        } else {
            transferContent = CCDTransferContent.builder()
                .dateOfTransfer(LocalDate.now()).build();
        }
        return ccdCase.toBuilder()
            .transferContent(transferContent)
            .build();

    }

    private void sendEmailNotifications(CCDCase ccdCase, Claim claim) {

        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        if (isDefendantLinked(ccdCase)) {
            transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);
        }
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }

    private void updateCaseInCCD(CCDCase ccdCase, String authorisation) {
        coreCaseDataService.update(authorisation, ccdCase, CaseEvent.AUTOMATED_TRANSFER);
    }
}
