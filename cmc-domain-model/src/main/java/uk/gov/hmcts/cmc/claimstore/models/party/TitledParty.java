package uk.gov.hmcts.cmc.claimstore.models.party;

import java.util.Optional;

public interface TitledParty {
    Optional<String> getTitle();
}
