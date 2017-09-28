package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class CCJContentProvider {

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        return Collections.singletonMap("ccj",new CCJContent( claim ));
    }
}
