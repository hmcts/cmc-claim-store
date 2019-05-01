package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediationCSVServiceTest {
    private static final String FROM_ADDRESS = "sender@mail.com";
    private static final String TO_ADDRESS = "recipient@mail.com";
    private static final String AUTHORISATION = "Authorisation";
    private static final String SAMPLE_CSV = "I,Am,A,Teapot";

    @Mock
    private EmailService emailService;
    @Mock
    private MediationCSVGenerator mediationCSVGenerator;
    @Mock
    private UserService userService;
    @Captor
    private ArgumentCaptor<EmailData> emailDataCaptor;

    private MediationCSVService service;

    @Before
    public void setUp() {
        this.service = new MediationCSVService(
            emailService,
            mediationCSVGenerator,
            userService,
            TO_ADDRESS,
            FROM_ADDRESS
        );
        when(mediationCSVGenerator.createMediationCSV(Mockito.anyString(), Mockito.any(LocalDate.class)))
            .thenReturn(SAMPLE_CSV);
    }

    @Test
    public void shouldPrepareCSVDataAndInvokeEmailService() throws IOException {
        service.sendMediationCSV(AUTHORISATION, LocalDate.now());

        verify(mediationCSVGenerator).createMediationCSV(AUTHORISATION, LocalDate.now());
        verifyEmailData();
    }

    @Test
    public void automatedCSVShouldUseYesterdayAndAnonymousUser() throws IOException {
        final User mockUser = mock(User.class);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(mockUser);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);

        service.automatedMediationCSV();

        verify(userService).authenticateAnonymousCaseWorker();
        verify(mediationCSVGenerator).createMediationCSV(AUTHORISATION, LocalDate.now().minusDays(1));

        verifyEmailData();
    }

    private static String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void verifyEmailData() throws IOException {
        verify(emailService).sendEmail(eq(FROM_ADDRESS), emailDataCaptor.capture());

        EmailData emailData = emailDataCaptor.getValue();
        assertThat(emailData.getSubject()).startsWith("MediationCSV");
        assertThat(emailData.getMessage()).startsWith("OCMC mediation");
        assertThat(emailData.getTo()).isEqualTo(TO_ADDRESS);

        assertThat(emailData.getAttachments()).hasSize(1);
        EmailAttachment attachment = emailData.getAttachments().get(0);
        assertThat(attachment.getFilename()).startsWith("MediationCSV");
        assertThat(attachment.getFilename()).endsWith(".csv");
        assertThat(attachment.getContentType()).isEqualTo("text/csv");

        assertThat(inputStreamToString(attachment.getData().getInputStream())).isEqualTo(SAMPLE_CSV);
    }
}
