package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.elasticsearch.common.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.time.LocalDate.now;

@Service
public class BulkPrintTransferService {

    private final Logger logger = LoggerFactory.getLogger(BulkPrintService.class);

    private CaseSearchApi caseSearchApi;

    private UserService userService;

    private CaseMapper caseMapper;
    private final TransferCaseDocumentPublishService transferCaseDocumentPublishService;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final CoreCaseDataService coreCaseDataService;
    private static final String REASON = "For directions";

    @Value("${automated_transfer_read_mode}")
    private boolean automatedTransferReadMode;

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

        Integer totalClaimsReadyForTransfer = caseSearchApi.totalClaimsReadyForTransfer(user);
        logger.info("Automated Transfer - Total cases in transfer ready state: " + totalClaimsReadyForTransfer);

        Map<CCDCase, Claim> claims = new HashMap<>();

        claimsReadyForTransfer(user, claims, "data.hearingCourtName", "data.hearingCourtAddress",
            x -> x.getHearingCourtName(), x-> x.getHearingCourtAddress());

        claimsReadyForTransfer(user, claims,
            "data.directionOrder.hearingCourtName", "data.directionOrder.hearingCourtAddress",
            x -> x.getDirectionOrder().getHearingCourtName(), x-> x.getDirectionOrder().getHearingCourtAddress());

        if(automatedTransferReadMode)
            return;

        claims.forEach((ccdCase, claim) -> {
            try {
                ccdCase = transferCase(ccdCase, claim, authorisation,
                    transferCaseDocumentPublishService::publishCaseDocuments,
                    transferCaseNotificationsService::sendTransferToCourtEmail, this::updateCaseData);
                updateCaseInCCD(ccdCase, authorisation);
                logger.info("Automated Transfer - " + ccdCase.getPreviousServiceCaseReference() + " done!");
            } catch (Exception e) {
                logger.error("Automated Transfer failed for: " + ccdCase.getPreviousServiceCaseReference());
            }
        });

        logger.info("Automated Transfer - completed !!");
    }

    private void claimsReadyForTransfer(User user, Map<CCDCase, Claim> claims,
                                        String courtNameKey, String courtAddressKey,
                                        Function<CCDCase,String> courtName, Function<CCDCase,CCDAddress> courtAddress) {

        List<CCDCase> claimsReadyForTransfer = caseSearchApi.getClaimsReadyForTransfer(
            user, courtNameKey, courtAddressKey);

        StringBuilder sb = new StringBuilder(format("Automated Transfer for %d cases where %s and %s found: ",
            claimsReadyForTransfer.size(), courtNameKey, courtAddressKey));

        if (! CollectionUtils.isEmpty(claimsReadyForTransfer)) {
            for (CCDCase ccdCase : claimsReadyForTransfer) {
                sb.append(ccdCase.getPreviousServiceCaseReference()).append(", ");
                updateTransferContent(ccdCase, claims, courtName, courtAddress);
            }
        }
        logger.info(sb.toString());
    }

    public CCDCase transferCase(CCDCase ccdCase, Claim claim, String authorisation,
                                TriFunction<CCDCase, String, Claim, CCDCase> transferCaseDocumentPublishService,
                                BiConsumer<CCDCase, Claim> sendEmailNotifications,
                                UnaryOperator<CCDCase> updateCaseData) {

        CCDCase updatedCase = transferCaseDocumentPublishService.apply(ccdCase, authorisation, claim);
        sendEmailNotifications.accept(updatedCase, claim);
        return updateCaseData.apply(updatedCase);
    }

    private void updateTransferContent(CCDCase ccdCase, Map<CCDCase, Claim> claims,
                                       Function<CCDCase,String> courtName, Function<CCDCase,CCDAddress> courtAddress) {

        CCDTransferContent transferContent = CCDTransferContent.builder()
            .transferCourtName(courtName.apply(ccdCase))
            .transferCourtAddress(courtAddress.apply(ccdCase))
            .transferReason(CCDTransferReason.OTHER)
            .transferReasonOther(REASON)
            .build();

        ccdCase =  ccdCase.toBuilder()
            .transferContent(transferContent)
            .build();

        claims.put(ccdCase, caseMapper.from(ccdCase));
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
