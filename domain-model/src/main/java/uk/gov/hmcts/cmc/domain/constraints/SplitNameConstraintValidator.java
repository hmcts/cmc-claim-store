
package uk.gov.hmcts.cmc.domain.constraints;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.SplitNamedParty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// todo ROC-5160 delete this class once frontend is merged

public class SplitNameConstraintValidator implements ConstraintValidator<SplitName, TheirDetails> {

    @Override
    public boolean isValid(TheirDetails theirDetails, ConstraintValidatorContext context) {
        if (theirDetails instanceof SplitNamedParty) {
            SplitNamedParty splitNamedParty = (SplitNamedParty) theirDetails;
            return !StringUtils.isBlank(theirDetails.getName())
                || (!StringUtils.isAnyBlank(splitNamedParty.getFirstName(), splitNamedParty.getLastName()));
        }
        return false;
    }
}
