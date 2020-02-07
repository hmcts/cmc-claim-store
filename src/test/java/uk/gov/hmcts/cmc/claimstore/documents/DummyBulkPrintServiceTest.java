package uk.gov.hmcts.cmc.claimstore.documents;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DummyBulkPrintServiceTest {
    private final Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Before
    public void setUp() {
        log.addAppender(mockAppender);
    }

    @After
    public void tearDown() {
        log.detachAppender(mockAppender);
    }

    @Test
    public void shouldLogMessageOnPrintCall() {
        Map<String, Object> pinContents = new HashMap<>();
        Document defendantLetterDocument = new Document("pinTemplate", pinContents);
        Map<String, Object> claimContents = new HashMap<>();
        Document sealedClaimDocument = new Document("sealedClaimTemplate", claimContents);

        new DummyBulkPrintService().print(
            SampleClaim.getDefault(),
            ImmutableList.of(
                new PrintableTemplate(defendantLetterDocument, "filename"),
                new PrintableTemplate(sealedClaimDocument, "filename")
            ));
        assertWasLogged("No bulk print operation need to be performed as 'Bulk print url' is switched off.");
    }

    private void assertWasLogged(CharSequence text) {
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getFormattedMessage()).contains(text);
    }

}
