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
class InterlocutoryJudgmentStaffNotificationServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider;
    @Mock
    private StaffPdfCreatorService staffPdfCreatorService;
    @Mock
    private InputStreamSource inputStreamSource;

    private InterlocutoryJudgmentStaffNotificationService service;

    private Claim claim;

    @DisplayName("staff_emails_enabled toggled off ")
    @Nested
    class StaffEmailsOffTests {

        @BeforeEach
        void setUp() {
            service = new InterlocutoryJudgmentStaffNotificationService(emailService, staffEmailProperties,
                staffPdfCreatorService, reDeterminationNotificationEmailContentProvider, false);

            claim = SampleClaim.getCitizenClaim();
        }

        @Test
        void notifyStaffInterlocutoryJudgmentSubmittedShouldNotSendEmail() {
            service.notifyStaffInterlocutoryJudgmentSubmitted(claim);

            verify(emailService, never()).sendEmail(any(), any());
        }
    }

    @DisplayName("staff_emails_enabled toggled on ")
    @Nested
    class StaffEmailsOnTests {

        @BeforeEach
        void setUp() {
            service
                = new InterlocutoryJudgmentStaffNotificationService(emailService, staffEmailProperties,
                staffPdfCreatorService, reDeterminationNotificationEmailContentProvider, true);
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
            void notifyStaffInterlocutoryJudgmentSubmittedShouldSendEmail() {
                service.notifyStaffInterlocutoryJudgmentSubmitted(claim);

                verify(emailService).sendEmail(any(), any());
            }

        }

        @DisplayName("Email Not Sent Tests")
        @Nested
        class EmailNotSentTests {

            @Test
            void notifyStaffInterlocutoryJudgmentSubmittedShouldRejectNullClaim() {
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffInterlocutoryJudgmentSubmitted(null));
            }

            @Test
            void notifyStaffInterlocutoryJudgmentSubmittedShouldRejectNullClaimantRespondedAt() {
                claim = SampleClaim.builder().build();
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffInterlocutoryJudgmentSubmitted(claim));
            }
        }
    }
}
