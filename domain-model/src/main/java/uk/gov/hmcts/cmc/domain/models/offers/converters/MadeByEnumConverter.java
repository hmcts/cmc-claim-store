package uk.gov.hmcts.cmc.domain.models.offers.converters;

import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import java.beans.PropertyEditorSupport;

public class MadeByEnumConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        setValue(MadeBy.valueOf(text.toUpperCase()));
    }

}
