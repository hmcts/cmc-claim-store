package uk.gov.hmcts.cmc.ccd.migration.ccd.repositories;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.models.mappers.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CCDCaseApi.class);

    public CCDCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
    }

    public Optional<Claim> getByReferenceNumber(String authorisation, String referenceNumber) {
        LOGGER.info("Get claim from CCD " + referenceNumber);
        User user = userService.getUser(authorisation);

        List<Claim> claims = extractClaims(
            search(user, ImmutableMap.of("case.referenceNumber", referenceNumber))
        );

        if (claims.size() > 1) {
            throw new RuntimeException("More than one claim found by claim reference " + referenceNumber);
        }

        LOGGER.info("Claim found " + claims.size());

        return claims.isEmpty() ? Optional.empty() : Optional.of(claims.get(0));
    }

    private List<CaseDetails> search(User user, Map<String, Object> searchString) {

        String serviceAuthToken = this.authTokenGenerator.generate();

        List<CaseDetails> result;
        result = this.coreCaseDataApi.searchForCaseworker(
            user.getAuthorisation(),
            serviceAuthToken,
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            searchString
        );

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
