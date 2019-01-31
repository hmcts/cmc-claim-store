package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SearchRepository {
    List<Claim> getAllCasesBy(User user, ImmutableMap<String, String> searchString);

    Optional<Claim> getCaseBy(String authorisation, Map<String, String> searchString);

    List<CaseDetails> searchAll(User user, Map<String, String> searchString);
}
