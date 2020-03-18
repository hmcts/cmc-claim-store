package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ReDeterminationNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CCJStaffNotificationServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider;
    @Mock
    private ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider;
    @Mock
    private StaffPdfCreatorService staffPdfCreatorService;

    private CCJStaffNotificationService service;

    private Claim claim;

    @DisplayName("staff_emails_enabled toggled off ")
    @Nested
    class StaffEmailsOffTests {

        @BeforeEach
        void setUp() {
            service = new CCJStaffNotificationService(emailService, staffEmailProperties,
                ccjRequestSubmittedEmailContentProvider, reDeterminationNotificationEmailContentProvider,
                staffPdfCreatorService, false);

            claim = SampleClaim.getCitizenClaim();
        }

        @Test
        void notifyStaffCCJRequestSubmittedShouldNotSendEmail() {
            service.notifyStaffCCJRequestSubmitted(claim);

            verify(emailService, never()).sendEmail(any(), any());
        }
    }

    @DisplayName("staff_emails_enabled toggled on ")
    @Nested
    class StaffEmailsOnTests {

        @BeforeEach
        void setUp() {
            service
                = new CCJStaffNotificationService(emailService, staffEmailProperties,
                ccjRequestSubmittedEmailContentProvider, reDeterminationNotificationEmailContentProvider,
                staffPdfCreatorService, true);
        }

        @DisplayName("Email Sent Tests")
        @Nested
        class EmailSentTests {

            @BeforeEach
            void setUp() {
                claim = SampleClaim.getDefault();

                when(ccjRequestSubmittedEmailContentProvider.createContent(any()))
                    .thenReturn(new EmailContent("", ""));
            }

            @Test
            void notifyStaffCCJRequestSubmittedShouldSendEmail() {
                service.notifyStaffCCJRequestSubmitted(claim);

                verify(emailService).sendEmail(any(), any());
            }

        }

        @DisplayName("Email Not Sent Tests")
        @Nested
        class EmailNotSentTests {

            @Test
            void notifyStaffCCJRequestSubmittedShouldRejectNullClaim() {
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffCCJRequestSubmitted(null));
            }

            @Test
            void notifyStaffCCJRequestSubmittedShouldRejectNullClaimantRespondedAt() {
                claim = SampleClaim.builder().build();
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffCCJRequestSubmitted(claim));
            }
        }
    }
}
