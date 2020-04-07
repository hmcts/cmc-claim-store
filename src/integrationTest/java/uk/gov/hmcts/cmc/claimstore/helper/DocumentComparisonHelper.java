package uk.gov.hmcts.cmc.claimstore.helper;

import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.util.Map;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class DocumentComparisonHelper {

    @Autowired
    private HTMLTemplateProcessor htmlTemplateProcessor;

    public PDFServiceClient provideLocalPdfService() {

        PDFServiceClient pdfServiceClient = mock(PDFServiceClient.class);

        when(pdfServiceClient.generateFromHtml(any(), any()))
            .thenAnswer((Answer<byte[]>) invocation -> {
                Object[] args = invocation.getArguments();
                byte[] template = (byte[]) args[0];
                Map<String, Object> placeholders = (Map<String, Object>) args[1];

                String output = htmlTemplateProcessor.process(new String(template), placeholders);
                return output.getBytes();
            });

        return pdfServiceClient;
    }

    public String replaceTimestamp(String timestampElementId, String html) {

        final String timestampElement = String.format("span id=\"%s\"", timestampElementId);
        final String regex = format("%s\\>.*\\<", timestampElement);
        final String replacement = format("%s\\>%s\\<", timestampElement, "TIMESTAMP");

        return html.replaceAll(regex, replacement);
    }
}
