package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.elasticsearch.common.TriFunction;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import static java.time.LocalDate.now;

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
        if (!claimsReadyForTransfer.isEmpty()) {
            claimsReadyForTransfer.forEach(claim -> ccdCaseMap.put(claim, caseMapper.to(claim)));
            ccdCaseMap.forEach((claim, ccdCase) -> {
                CCDCase ccdCaseTransferred = transferCase(ccdCase, claim, authorisation,
                    transferCaseDocumentPublishService::publishCaseDocuments,
                    transferCaseNotificationsService::sendTransferToCourtEmail, this::updateCaseData);
                CCDCase ccdCaseUpdated = updateTransferContent(ccdCaseTransferred);
                updateCaseInCCD(ccdCaseUpdated, authorisation);
            });
        }
    }

    public CCDCase transferCase(CCDCase ccdCase, Claim claim, String authorisation,
                                TriFunction<CCDCase, String, Claim, CCDCase> transferCaseDocumentPublishService,
                                BiConsumer<CCDCase, Claim> sendEmailNotifications,
                                UnaryOperator<CCDCase> updateCaseData) {

        CCDCase updatedCase = transferCaseDocumentPublishService.apply(ccdCase, authorisation, claim);
        sendEmailNotifications.accept(updatedCase, claim);
        return updateCaseData.apply(updatedCase);
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

    public CCDCase updateCaseDataWithHandOffDate(CCDCase ccdCase) {
        return ccdCase.toBuilder().dateOfHandoff(now()).build();
    }

    public CCDCase updateCaseData(CCDCase ccdCase) {

        CCDTransferContent transferContent;
        if (ccdCase.getTransferContent() != null) {
            transferContent = ccdCase.getTransferContent().toBuilder()
                .dateOfTransfer(now()).build();
        } else {
            transferContent = CCDTransferContent.builder()
                .dateOfTransfer(now()).build();
        }
        return ccdCase.toBuilder()
            .transferContent(transferContent)
            .build();

    }

    private void updateCaseInCCD(CCDCase ccdCase, String authorisation) {
        coreCaseDataService.update(authorisation, ccdCase, CaseEvent.AUTOMATED_TRANSFER);
    }
}
