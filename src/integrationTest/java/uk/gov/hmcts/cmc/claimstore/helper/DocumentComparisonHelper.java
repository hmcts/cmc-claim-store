package uk.gov.hmcts.cmc.claimstore.helper;

import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class DocumentComparisonHelper {

    public String replaceTimestamp(String timestampElementId, String html) {

        final String timestampElement = String.format("data-type=\"%s\"", timestampElementId);
        final String regex = format("%s\\>.*\\<", timestampElement);
        final String replacement = format("%s\\>%s\\<", timestampElement, "TIMESTAMP");

        return html.replaceAll(regex, replacement);
    }
}
