package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Optional;

@Service
public class CCDDataAccessService {
    private static final Logger logger = LoggerFactory.getLogger(CCDDataAccessService.class);

    private final UserService userService;
    private final SearchCCDCaseService searchCCDCaseService;
    private final List<String> casesToSearch;

    public CCDDataAccessService(
        UserService userService,
        SearchCCDCaseService searchCCDCaseService,
        @Value("${migration.cases.references}") List<String> casesToSearch
    ) {
        this.userService = userService;
        this.searchCCDCaseService = searchCCDCaseService;
        this.casesToSearch = casesToSearch;
    }

    public void findCaseDetails() {
        User user = userService.authenticateAnonymousCaseWorker();
        casesToSearch.forEach(caseId -> {
            Optional<CaseDetails> caseDetails = searchCCDCaseService.getCcdCaseByReferenceNumber(user, caseId);
            if (caseDetails.isPresent()) {
                logger.info("case details returned with query Param for {}, has id {}", caseId,
                    caseDetails.get().getId());
            } else {
                logger.info("No case details returned with query Param for {} ", caseId);
            }

            Optional<CCDCase> caseDetailsWithoutQueryParam
                = searchCCDCaseService.getCcdCaseByReferenceNumberWithoutFilterParam(user, caseId);

            if (caseDetailsWithoutQueryParam.isPresent()) {

                logger.info("case details returned without query Param for {}, has id {}",
                    caseId,
                    caseDetailsWithoutQueryParam.get().getId());
            } else {
                logger.info("No case details returned without query Param for {} ", caseId);
            }

        });

    }
}
