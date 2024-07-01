package uk.gov.hmcts.cmc.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.apache.http.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.email.EmailSendFailedException;
import uk.gov.hmcts.cmc.email.SampleEmailData;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendGridClientTest {
    private static final String API_KEY = "dummy key";

    private static final Response GOOD_RESPONSE = new Response(202, "response body", Collections.emptyMap());

    @Mock
    private SendGridFactory factory;

    @Mock
    private SendGrid sendGrid;

    @Captor
    private ArgumentCaptor<Request> requestCaptor;

    private SendGridClient sendGridClient;

    @BeforeEach
    public void setUp() {
        when(factory.createSendGrid(API_KEY, true)).thenReturn(sendGrid);
        sendGridClient = new SendGridClient(factory, API_KEY, true);
    }

    @Test
    public void testRequestContents() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(202, "response body", Collections.emptyMap()));

        sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getDefault());

        verify(sendGrid).api(requestCaptor.capture());

        Request capturedRequest = requestCaptor.getValue();
        assertEquals(Method.POST, capturedRequest.getMethod());
        assertEquals("mail/send", capturedRequest.getEndpoint());
        assertTrue(capturedRequest.getBody().contains("\"email\":\"" + SampleEmailData.EMAIL_FROM + "\""));
    }

    @Test
    public void testNon2xxResponseThrowsException() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(400, "bad request", Collections.emptyMap()));
        try {
            sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getDefault());
        } catch (Exception e) {
            assertTrue(e instanceof EmailSendFailedException);
            EmailSendFailedException emailSendFailedException = (EmailSendFailedException) e;
            Throwable causeThrowable = emailSendFailedException.getCause();
            assertTrue(causeThrowable instanceof HttpException);
            HttpException cause = (HttpException) causeThrowable;
            assertEquals("SendGrid returned a non-success response (400); body: bad request", cause.getMessage());
        }
    }

    @Test
    public void testAttachments() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(GOOD_RESPONSE);

        sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getWithAttachment("test.pdf"));

        verify(sendGrid).api(requestCaptor.capture());

        Request capturedRequest = requestCaptor.getValue();
        assertTrue(capturedRequest.getBody().contains("\"filename\":\"test.pdf\""));
    }

    @Test
    public void testIOExceptionIsPropagated() throws IOException {
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));
        assertThrows(IOException.class, () -> {
            sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getDefault());
        });
    }

    @Test
    public void testNullFromNotAllowed() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            sendGridClient.sendEmail(null, SampleEmailData.getDefault());
        });
    }

    @Test
    public void testBlankFromNotAllowed() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            sendGridClient.sendEmail(" \t ", SampleEmailData.getDefault());
        });
    }

    @Test
    public void testNullToNotAllowed() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getWithToNull());
        });
    }

    @Test
    public void testNullSubjectNotAllowed() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getWithSubjectNull());
        });
    }

    @Test
    public void testEmptyContent() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(GOOD_RESPONSE);
        sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getWithEmptyContent());
        verify(sendGrid).api(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();
        assertTrue(capturedRequest.getBody().contains("\"content\":[{\"type\":\"text/plain\",\"value\":\" \"}]"));
    }
}
