package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ChangeContactDetailsRule {
    public static final String PARTY_SELECTED =
            "Select a party";

    public List<String> validateExpectedFieldsFilledByCaseworker(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "ccd case object can not be null");
        List<String> validationErrors = new ArrayList<>();

            if (!isPresent(ccdCase.getChangeContactDetailsForParty())
            ) {
                validationErrors.add(PARTY_SELECTED);
            } else {
                //check for any mandatory fields being empty
            }
        return validationErrors;
    }

    private boolean isPresent(CCDYesNoOption input) {
        return Optional.ofNullable(input).isPresent();
    }
}
