package uk.gov.hmcts.cmc.claimstore.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Query;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;

@Service("searchRepository")
public class CCDElasticSearchRepository {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseDetailsConverter ccdCaseDetailsConverter;

    @Autowired
    public CCDElasticSearchRepository(CoreCaseDataApi coreCaseDataApi,
                                      AuthTokenGenerator authTokenGenerator,
                                      UserService userService,
                                      CaseDetailsConverter ccdCaseDataToClaim) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.ccdCaseDetailsConverter = ccdCaseDataToClaim;
    }

    private List<Claim> searchClaimsWith(User user,
                                         Query query) {

        String serviceAuthToken = this.authTokenGenerator.generate();

        return coreCaseDataApi.searchCases(
            user.getAuthorisation(),
            serviceAuthToken,
            CASE_TYPE_ID,
            query.toString()
        ).getCases()
            .stream()
            .map(ccdCaseDetailsConverter::extractClaim)
            .collect(Collectors.toList());
    }
}
