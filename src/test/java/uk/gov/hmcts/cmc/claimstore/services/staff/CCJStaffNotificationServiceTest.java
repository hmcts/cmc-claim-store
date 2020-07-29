package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamSource;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ReDeterminationNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
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
    @Mock
    private InputStreamSource inputStreamSource;

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
        void notifyStaffCCJReDeterminationRequestShouldNotSendEmail() {
            service.notifyStaffCCJReDeterminationRequest(claim, "");

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
                claim = SampleClaim.getLegalDataWithReps();

                when(staffPdfCreatorService.createSealedClaimPdfAttachment(claim))
                    .thenReturn(new EmailAttachment(inputStreamSource, "", "createSealedClaimPdfAttachment"));
                when(staffPdfCreatorService.createResponsePdfAttachment(claim))
                    .thenReturn(new EmailAttachment(inputStreamSource, "", "createResponsePdfAttachment"));
                when(reDeterminationNotificationEmailContentProvider.createContent(any()))
                    .thenReturn(new EmailContent("", ""));
            }

            @Test
            void notifyStaffCCJReDeterminationRequestShouldSendEmail() {
                service.notifyStaffCCJReDeterminationRequest(claim, "");

                verify(emailService).sendEmail(any(), any());
            }

        }

        @DisplayName("Email Not Sent Tests")
        @Nested
        class EmailNotSentTests {

            @Test
            void notifyStaffCCJReDeterminationRequestShouldRejectNullClaim() {
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffCCJReDeterminationRequest(null, ""));
            }

            @Test
            void notifyStaffCCJReDeterminationRequestShouldRejectNullClaimantRespondedAt() {
                claim = SampleClaim.builder().build();
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffCCJReDeterminationRequest(claim, ""));
            }
        }
    }
}
