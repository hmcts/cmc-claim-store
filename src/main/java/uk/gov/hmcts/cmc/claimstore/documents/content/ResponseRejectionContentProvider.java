package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class ResponseRejectionContentProvider {

    private static final String DEFENCE_FORM_NO = "OCON9B";

    public Map<String, Object> createContent(ResponseRejection responseRejection) {
        requireNonNull(responseRejection);
        Map<String, Object> content = new HashMap<>();

        responseRejection.getReason().ifPresent(reason -> content.put("rejectionReason", reason));
        content.put("freeMediation", responseRejection
            .getFreeMediation()
            .orElse(YesNoOption.NO).name());
        content.put("formNumber", DEFENCE_FORM_NO);
        return content;
    }
}
