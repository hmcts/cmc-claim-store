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
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantRejectOrgPaymentPlanStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantRejectOrgPaymentPlanStaffNotificationServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private StaffPdfCreatorService staffPdfCreatorService;
    @Mock
    private ClaimantRejectOrgPaymentPlanStaffEmailContentProvider claimantRejectOrgPaymentPlanStaffEmailContentProvider;
    @Mock
    private InputStreamSource inputStreamSource;

    private ClaimantRejectOrgPaymentPlanStaffNotificationService service;

    private Claim claim;

    @DisplayName("staff_emails_enabled toggled off ")
    @Nested
    class StaffEmailsOffTests {

        @BeforeEach
        void setUp() {
            service = new ClaimantRejectOrgPaymentPlanStaffNotificationService(emailService, staffEmailProperties,
                staffPdfCreatorService, claimantRejectOrgPaymentPlanStaffEmailContentProvider, false);

            claim = SampleClaim.getCitizenClaim();
        }

        @Test
        void notifyStaffClaimantRejectOrganisationPaymentPlanShouldNotSendEmail() {
            service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);

            verify(emailService, never()).sendEmail(any(), any());
        }
    }

    @DisplayName("staff_emails_enabled toggled on ")
    @Nested
    class StaffEmailsOnTests {

        @BeforeEach
        void setUp() {
            service
                = new ClaimantRejectOrgPaymentPlanStaffNotificationService(emailService, staffEmailProperties,
                staffPdfCreatorService, claimantRejectOrgPaymentPlanStaffEmailContentProvider, true);
        }

        @DisplayName("Email Sent Tests")
        @Nested
        class EmailSentTests {

            @BeforeEach
            void setUp() {
                claim = SampleClaim.builder().withClaimantRespondedAt(LocalDateTime.now()).build();

                when(staffPdfCreatorService.createSealedClaimPdfAttachment(claim))
                    .thenReturn(new EmailAttachment(inputStreamSource, "", "createSealedClaimPdfAttachment"));
                when(staffPdfCreatorService.createResponsePdfAttachment(claim))
                    .thenReturn(new EmailAttachment(inputStreamSource, "", "createResponsePdfAttachment"));
                when(staffPdfCreatorService.createClaimantResponsePdfAttachment(claim))
                    .thenReturn(new EmailAttachment(inputStreamSource, "", "createClaimantResponsePdfAttachment"));
                when(claimantRejectOrgPaymentPlanStaffEmailContentProvider.createContent(any()))
                    .thenReturn(new EmailContent("", ""));
            }

            @Test
            void notifyStaffClaimantRejectOrganisationPaymentPlanShouldSendEmail() {
                service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);

                verify(emailService).sendEmail(any(), any());
            }

        }

        @DisplayName("Email Not Sent Tests")
        @Nested
        class EmailNotSentTests {

            @Test
            void notifyStaffClaimantRejectOrganisationPaymentPlanShouldRejectNullClaim() {
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffClaimantRejectOrganisationPaymentPlan(null));

            }

            @Test
            void notifyStaffClaimantRejectOrganisationPaymentPlanShouldRejectNullClaimantRespondedAt() {
                claim = SampleClaim.builder().build();
                Assertions.assertThrows(NullPointerException.class,
                    () -> service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim));

            }
        }
    }
}
