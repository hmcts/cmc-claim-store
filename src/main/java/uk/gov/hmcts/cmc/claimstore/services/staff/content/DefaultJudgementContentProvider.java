package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Collections;
import java.util.Map;

@Component
public class DefaultJudgementContentProvider {
    //TODO Data for default judgmenrt PDF

    public Map<String,Object> createContent(Claim claim) {
        return Collections.emptyMap();
    }
}
