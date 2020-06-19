package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.CoverLetterGenerator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseLetterSender;
import uk.gov.hmcts.cmc.domain.models.Claim;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;

@Service
public class BulkPrintTransferService {

    private CaseSearchApi caseSearchApi;

    private UserService userService;

    private final TransferCaseLetterSender transferCaseLetterSender;

    private CaseMapper caseMapper;
    private final String courtLetterTemplateId;
    private final CoverLetterGenerator coverLetterGenerator;

    @Autowired
    public BulkPrintTransferService(
        CaseSearchApi caseSearchApi,
        UserService userService,
        TransferCaseLetterSender transferCaseLetterSender,
        CaseMapper caseMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String courtLetterTemplateId,
        CoverLetterGenerator coverLetterGenerator
    ) {
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.caseMapper = caseMapper;
        this.courtLetterTemplateId = courtLetterTemplateId;
        this.coverLetterGenerator = coverLetterGenerator;
    }

    public void bulkPrintTransfer() {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        List<Claim> claimsReadyForTransfer = caseSearchApi
            .getClaimsReadyForTransfer(user);
        Map<Claim, CCDCase> ccdCaseMap = new HashMap<>();
        claimsReadyForTransfer.forEach(claim -> ccdCaseMap.put(claim, caseMapper.to(claim)));
        ccdCaseMap.forEach((claim, ccdCase) -> transferCaseLetterSender
            .sendAllCaseDocumentsToCourt(
                user.getAuthorisation(),
                ccdCase,
                claim, coverLetterGenerator
                    .generate(ccdCase, authorisation, FOR_COURT, courtLetterTemplateId)));
    }
}
