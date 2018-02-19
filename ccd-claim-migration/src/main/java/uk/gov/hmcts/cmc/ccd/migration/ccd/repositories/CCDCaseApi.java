package uk.gov.hmcts.cmc.ccd.migration.ccd.repositories;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.models.mappers.JsonMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseApi {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final JsonMapper jsonMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CCDCaseApi.class);

    public CCDCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        JsonMapper jsonMapper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
    }

    public boolean claimExists(User user, String referenceNumber) {
        LOGGER.info("Get claim from CCD " + referenceNumber);

        int n = search(user, ImmutableMap.of("case.referenceNumber", referenceNumber));

        if (n > 1) {
            throw new RuntimeException("More than one claim found by claim reference " + referenceNumber);
        }

        LOGGER.info("Claim found " + n);

        return n > 0;
    }

    private int search(User user, Map<String, Object> searchString) {

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

        return result != null ? result.size() : 0;
    }
}
