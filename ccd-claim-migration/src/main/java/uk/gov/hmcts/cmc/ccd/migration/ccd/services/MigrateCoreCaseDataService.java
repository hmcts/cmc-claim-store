package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import static uk.gov.hmcts.cmc.ccd.migration.ccd.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.ccd.migration.ccd.repositories.CCDCaseApi.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class MigrateCoreCaseDataService {

    private static final Logger logger = LoggerFactory.getLogger(MigrateCoreCaseDataService.class);

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final UserService userService;
    private final CaseMapper caseMapper;

    @Autowired
    public MigrateCoreCaseDataService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        UserService userService,
        CaseMapper caseMapper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseAccessApi = caseAccessApi;
        this.userService = userService;
        this.caseMapper = caseMapper;
    }

    public void update(
        String authorisation,
        EventRequestData eventRequestData,
        Claim claim
    ) {
        logger.info("claim: " + claim);
        CCDCase ccdCase = caseMapper.to(claim);
        logger.info("ccdCase: " + ccdCase);
        StartEventResponse startEventResponse = start(authorisation, eventRequestData);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build()
            ).data(ccdCase)
            .build();

        submit(authorisation, eventRequestData, caseDataContent);
    }

    public void save(
        String authorisation, EventRequestData eventRequestData, Claim claim
    ) {
        CCDCase ccdCase = caseMapper.to(claim);

        StartEventResponse startEventResponse = start(authorisation, eventRequestData);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case submission event")
                    .description("Submitting CMC case")
                    .build()
            ).data(ccdCase)
            .build();

        CaseDetails caseDetails = submit(authorisation, eventRequestData, caseDataContent);

        String defendantId = claim.getDefendantId() != null ? claim.getDefendantId() : claim.getLetterHolderId();

        // make sure both submitter and defendant (or letterHolder) can access the case
        grantAccess(caseDetails.getId().toString(), claim.getSubmitterId());
        grantAccess(caseDetails.getId().toString(), defendantId);
    }

    private void grantAccess(
        String caseId, String userId
    ) {
        User user = userService.getUser(userService.authenticateAnonymousCaseworker());

        caseAccessApi.grantAccessToCase(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            new UserId(userId)
        );
    }

    private CaseDetails submit(
        String authorisation, EventRequestData eventRequestData, CaseDataContent caseDataContent
    ) {
        return coreCaseDataApi.submitForCaseworker(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }

    private StartEventResponse start(String authorisation, EventRequestData eventRequestData) {
        return this.coreCaseDataApi.startForCaseworker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );
    }
}
