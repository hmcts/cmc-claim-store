package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CCJContentProvider {
    //TODO Data for default judgmenrt PDF

    public Map<String,Object> createContent(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claim",claim);
        return map;
    }
}
