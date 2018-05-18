package uk.gov.hmcts.cmc.domain.statementofmeans;

import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

public class Employment {

    private final YesNoOption isCurrentlyEmployed;
    private final boolean employed;
    private final boolean selfEmployed;

    public Employment(YesNoOption isCurrentlyEmployed, boolean employed, boolean selfEmployed) {
        this.isCurrentlyEmployed = isCurrentlyEmployed;
        this.employed = employed;
        this.selfEmployed = selfEmployed;
    }
}
