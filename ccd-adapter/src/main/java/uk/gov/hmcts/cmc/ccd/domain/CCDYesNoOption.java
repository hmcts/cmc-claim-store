package uk.gov.hmcts.cmc.ccd.domain;

import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

public enum CCDYesNoOption {
    YES("yes"),
    NO("no");

    private String value;

    CCDYesNoOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean toBoolean() {
        return this == YES;
    }

    public static CCDYesNoOption valueOf(boolean value) {
        return value ? YES : NO;
    }

    public static CCDYesNoOption mapFrom(YesNoOption value) {
        if (value == null) {
            return null;
        }

        return CCDYesNoOption.valueOf(value.name());
    }

    public static String name(CCDYesNoOption value) {
        if (value == null) {
            return null;
        }

        return value.name();
    }
}
