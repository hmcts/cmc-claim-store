package uk.gov.hmcts.cmc.domain.utils;

import org.apache.commons.lang3.builder.StandardToStringStyle;

public final class ToStringStyle {
    private ToStringStyle() {
        // Utility class
    }

    public static StandardToStringStyle ourStyle() {
        StandardToStringStyle standardToStringStyle = new StandardToStringStyle();
        standardToStringStyle.setUseClassName(false);
        standardToStringStyle.setUseIdentityHashCode(false);
        standardToStringStyle.setContentStart("[");
        standardToStringStyle.setFieldSeparator(System.lineSeparator() + "  ");
        standardToStringStyle.setFieldSeparatorAtStart(true);
        standardToStringStyle.setContentEnd(System.lineSeparator() + "]");
        return standardToStringStyle;
    }
}
