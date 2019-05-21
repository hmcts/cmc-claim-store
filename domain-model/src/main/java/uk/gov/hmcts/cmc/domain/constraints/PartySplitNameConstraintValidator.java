
package uk.gov.hmcts.cmc.domain.constraints;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SplitNamedParty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// todo ROC-5160 delete this class once frontend is merged

public class PartySplitNameConstraintValidator implements ConstraintValidator<PartySplitName, Party> {

    @Override
    public boolean isValid(Party party, ConstraintValidatorContext context) {
        if (party instanceof SplitNamedParty) {
            SplitNamedParty splitNamedParty = (SplitNamedParty) party;
            return !StringUtils.isBlank(party.getName())
                || (!StringUtils.isAnyBlank(splitNamedParty.getFirstName(), splitNamedParty.getLastName()));
        }
        return false;
    }
}
