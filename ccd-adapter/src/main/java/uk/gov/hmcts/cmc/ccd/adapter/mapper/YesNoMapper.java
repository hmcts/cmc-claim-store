package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
public class YesNoMapper implements Mapper<CCDYesNoOption, YesNoOption> {

    @Override
    public CCDYesNoOption to(YesNoOption yesNoOption) {
        if (yesNoOption == null) {
            return null;
        }
        return CCDYesNoOption.valueOf(yesNoOption.name());
    }

    @Override
    public YesNoOption from(CCDYesNoOption ccdYesNoOption) {
        if (ccdYesNoOption == null) {
            return null;
        }
        return YesNoOption.valueOf(ccdYesNoOption.name());
    }
}
