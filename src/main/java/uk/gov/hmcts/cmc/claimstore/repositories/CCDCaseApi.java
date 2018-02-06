package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JwtHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseApi {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final JwtHelper jwtHelper;
    private final CaseAccessApi caseAccessApi;

    private static final Logger LOGGER = LoggerFactory.getLogger(CCDCaseApi.class);

    public CCDCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        JwtHelper jwtHelper,
        CaseAccessApi caseAccessApi
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.jwtHelper = jwtHelper;
        this.caseAccessApi = caseAccessApi;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        User user = userService.getUser(authorisation);
        return extractClaims(search(user, ImmutableMap.of("case.submitterId", submitterId)));
    }

    public Optional<Claim> getByReferenceNumber(String referenceNumber, String authorisation) {
        User user = userService.getUser(authorisation);

        List<Claim> claims = extractClaims(
            search(user, ImmutableMap.of("case.referenceNumber", referenceNumber))
        );

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim reference " + referenceNumber);
        }

        return claims.isEmpty() ? Optional.empty() : Optional.of(claims.get(0));
    }

    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        User user = userService.getUser(authorisation);
        return getCaseDetailsByExternalId(user, externalId)
            .map(CaseDetails::getData)
            .map(this::convertToCCDCase)
            .map((caseMapper::from));
    }

    /**
     * LLD https://tools.hmcts.net/confluence/display/ROC/Defendant+linking+with+CCD
     */
    public void linkDefendant(String authorisation) {
        User defendantUser = userService.getUser(authorisation);
        List<String> letterHolderIds = defendantUser.getUserDetails().getRoles()
            .stream()
            .filter(this::isLetterHolderRole)
            .map(this::extractLetterHolderId)
            .collect(Collectors.toList());

        if (letterHolderIds.isEmpty()) {
            return;
        }

        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();

        letterHolderIds
            .forEach(letterHolderId -> caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
                anonymousCaseWorker.getAuthorisation(),
                authTokenGenerator.generate(),
                anonymousCaseWorker.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                letterHolderId
            ).forEach(caseId -> linkToCase(defendantUser, anonymousCaseWorker, letterHolderId, caseId)));
    }

    private void linkToCase(User defendantUser, User anonymousCaseWorker, String letterHolderId, String caseId) {
        String defendantId = defendantUser.getUserDetails().getId();
        LOGGER.info("Granting access to case: {} for user: {} with letter-holder id: {}",
            caseId, defendantId, letterHolderId);
        caseAccessApi.grantAccessToCase(anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            new UserId(defendantId)
        );

        LOGGER.info("Revoking access to case: {} for user: {}", caseId, letterHolderId);
        caseAccessApi.revokeAccessToCase(anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            letterHolderId
        );
    }

    private boolean isLetterHolderRole(String role) {
        Objects.requireNonNull(role);
        return role.startsWith("letter")
            && !role.equals("letter-holder")
            && !role.endsWith("loa1");
    }

    public List<Claim> getByDefendantId(String id, String authorisation) {
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
        List<String> caseIdsGivenUserIdHasAccessTo = caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
            anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            id
        );

        User defendant = userService.getUser(authorisation);
        return caseIdsGivenUserIdHasAccessTo.stream()
            .map(caseId -> readCase(defendant, caseId))
            .filter((claim -> !claim.getSubmitterId().equals(id)))
            .collect(Collectors.toList());
    }

    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();

        List<String> letterHolderCases = caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
            anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            id
        );
        if (letterHolderCases.size() > 1) {
            throw new DefendantLinkingException("More than 1 case a letter holder ID has access to found");
        }
        if (letterHolderCases.isEmpty()) {
            throw new DefendantLinkingException("No cases found that the letter holder ID has access to");
        }

        User letterHolder = userService.getUser(authorisation);
        return Optional.of(readCase(letterHolder, letterHolderCases.get(0)));
    }

    private String extractLetterHolderId(String role) {
        return StringUtils.remove(role, "letter-");
    }

    private Claim readCase(User user, String caseId) {
        return caseMapper.from(
            convertToCCDCase(
                coreCaseDataApi.readForCitizen(
                    user.getAuthorisation(),
                    authTokenGenerator.generate(),
                    user.getUserDetails().getId(),
                    JURISDICTION_ID,
                    CASE_TYPE_ID,
                    caseId
                ).getData()
            )
        );
    }

    private Optional<CaseDetails> getCaseDetailsByExternalId(User user, String externalId) {
        List<CaseDetails> caseResults = search(user, ImmutableMap.of("case.externalId", externalId));
        if (caseResults.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim externalId " + externalId);
        }

        return caseResults.isEmpty() ? Optional.empty() : Optional.of(caseResults.get(0));
    }

    private List<CaseDetails> search(User user, Map<String, Object> searchString) {

        String serviceAuthToken = this.authTokenGenerator.generate();

        List<CaseDetails> result;
        if (jwtHelper.isSolicitor(user.getAuthorisation())) {
            result = this.coreCaseDataApi.searchForCaseworker(
                user.getAuthorisation(),
                serviceAuthToken,
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

        } else {
            result = this.coreCaseDataApi.searchForCitizen(
                user.getAuthorisation(),
                serviceAuthToken,
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

        }
        return result;
    }

    private List<Claim> extractClaims(List<CaseDetails> result) {

        Map<Long, Map<String, Object>> collectMap = result.stream()
            .collect(Collectors.toMap(CaseDetails::getId, CaseDetails::getData));

        return collectMap.entrySet().stream()
            .map(this::mapToClaim)
            .collect(Collectors.toList());
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        String json = jsonMapper.toJson(mapData);
        return jsonMapper.fromJson(json, CCDCase.class);
    }

    private Claim mapToClaim(Map.Entry<Long, Map<String, Object>> entry) {
        Map<String, Object> entryValue = entry.getValue();
        entryValue.put("id", entry.getKey());

        CCDCase ccdCase = convertToCCDCase(entryValue);
        return caseMapper.from(ccdCase);
    }
}
