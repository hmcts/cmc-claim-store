package uk.gov.hmcts.cmc.claimstore.models.offers.converters;

import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;

import java.beans.PropertyEditorSupport;

public class MadeByEnumConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(MadeBy.valueOf(text.toUpperCase()));
    }

}
