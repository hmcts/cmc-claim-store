package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

import java.util.Optional;

public class LivingPartnerAssert extends CustomAssert<LivingPartnerAssert, LivingPartner> {

    public LivingPartnerAssert(LivingPartner actual) {
        super("LivingPartner", actual, LivingPartnerAssert.class);
    }

    public LivingPartnerAssert isEqualTo(CCDLivingPartner expected) {
        isNotNull();

        compare("disability",
            expected.getDisability(), Enum::name,
            Optional.ofNullable(actual.getDisability()).map(Enum::name));

        compare("over18",
            expected.getOver18(), CCDYesNoOption::toBoolean,
            Optional.of(actual.isOver18()));

        compare("pensioner",
            expected.getPensioner(), CCDYesNoOption::toBoolean,
            Optional.of(actual.isPensioner()));

        return this;
    }
}
