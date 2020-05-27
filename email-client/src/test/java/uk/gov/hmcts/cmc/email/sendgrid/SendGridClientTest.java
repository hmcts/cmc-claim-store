package uk.gov.hmcts.cmc.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.email.EmailSendFailedException;
import uk.gov.hmcts.cmc.email.SampleEmailData;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendGridClientTest {
    private static final String API_KEY = "dummy key";

    @Mock
    private SendGridFactory factory;

    @Mock
    private SendGrid sendGrid;

    @Captor
    private ArgumentCaptor<Request> requestCaptor;

    private SendGridClient sendGridClient;

    @Before
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

    @Test(expected = IOException.class)
    public void testIOExceptionIsPropagated() throws IOException {
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));
        sendGridClient.sendEmail(SampleEmailData.EMAIL_FROM, SampleEmailData.getDefault());
    }
}
