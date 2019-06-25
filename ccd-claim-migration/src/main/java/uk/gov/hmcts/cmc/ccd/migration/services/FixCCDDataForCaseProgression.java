package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FixCCDDataForCaseProgression {
    private static final Logger logger = LoggerFactory.getLogger(FixCCDDataForCaseProgression.class);
    private final SearchCCDCaseService searchCCDCaseService;
    private final SupportUpdateService supportUpdateService;
    private final JsonMapper jsonMapper;
    private final CaseMapper caseMapper;
    private final List<String> casesTofix;
    private final boolean dryRun;
    private final UserService userService;

    @Autowired
    public FixCCDDataForCaseProgression(
        UserService userService,
        SearchCCDCaseService searchCCDCaseService,
        SupportUpdateService supportUpdateService,
        JsonMapper jsonMapper,
        CaseMapper caseMapper,
        @Value("${migration.cases.references}") List<String> casesTofix,
        @Value("${migration.dryRun}") boolean dryRun
    ) {
        this.userService = userService;
        this.searchCCDCaseService = searchCCDCaseService;
        this.supportUpdateService = supportUpdateService;
        this.jsonMapper = jsonMapper;
        this.caseMapper = caseMapper;
        this.casesTofix = casesTofix;
        this.dryRun = dryRun;
    }

    public void fixData() {
        User user = userService.authenticateSystemUpdateUser();
        AtomicInteger failedOnUpdateMigrations = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);

        casesTofix.forEach(caseReference -> {

            Optional<CaseDetails> caseDetails = searchCCDCaseService.getCcdCaseByReferenceNumber(user, caseReference);
            caseDetails.ifPresent(c -> {

                CCDCase ccdCase = mapToCCDCase(c.getData(), Long.toString(c.getId()));
                Claim claim = caseMapper.from(ccdCase);
                CCDCase updatedCase = caseMapper.to(claim.toBuilder().defendantEmail(null).build());
                supportUpdateService.updateCase(user, updatedClaims, failedOnUpdateMigrations, updatedCase);

            });
        });
        logger.info("Successful updates: " + updatedClaims.toString());
    }

    private CCDCase mapToCCDCase(Map<String, Object> caseData, String caseId) {
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }
}
