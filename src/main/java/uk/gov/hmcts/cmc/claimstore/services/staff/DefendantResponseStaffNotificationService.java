package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.FullAdmissionStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.FullDefenceStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.PDF;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class DefendantResponseStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final FullDefenceStaffEmailContentProvider fullDefenceStaffEmailContentProvider;
    private final FullAdmissionStaffEmailContentProvider fullAdmissionStaffEmailContentProvider;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefendantResponseStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        FullDefenceStaffEmailContentProvider fullDefenceStaffEmailContentProvider,
        FullAdmissionStaffEmailContentProvider fullAdmissionStaffEmailContentProvider,
        DefendantResponseReceiptService defendantResponseReceiptService) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.fullDefenceStaffEmailContentProvider = fullDefenceStaffEmailContentProvider;
        this.fullAdmissionStaffEmailContentProvider = fullAdmissionStaffEmailContentProvider;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    public void notifyStaffDefenceSubmittedFor(
        Claim claim,
        String defendantEmail
    ) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalArgumentException::new).getResponseType();

        if (isPartAdmission(responseType)) {
            return;
        }

        EmailContent emailContent = isFullAdmission(responseType)
            ? fullAdmissionStaffEmailContentProvider.createContent(wrapInMap(claim, defendantEmail))
            : fullDefenceStaffEmailContentProvider.createContent(wrapInMap(claim, defendantEmail));

        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(createResponsePdfAttachment(claim))
            )
        );
    }

    private boolean isPartAdmission(ResponseType responseType) {
        return responseType == PART_ADMISSION;
    }

    private boolean isFullAdmission(ResponseType responseType) {
        return responseType == FULL_ADMISSION;
    }

    public static Map<String, Object> wrapInMap(
        Claim claim,
        String defendantEmail
    ) {
        Map<String, Object> map = new HashMap<>();

        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        map.put("claim", claim);
        map.put("response", response);
        map.put("defendantEmail", defendantEmail);
        map.put("defendantMobilePhone", response
            .getDefendant()
            .getMobilePhone()
            .orElse(null));
        map.put("responseDeadline", formatDate(claim.getResponseDeadline()));
        map.put("fourteenDaysFromNow", formatDate(now().plusDays(14)));

        if (response.getResponseType() == FULL_ADMISSION) {
            FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
            map.put("paymentOption", fullAdmissionResponse.getPaymentIntention().getPaymentOption());
            map.put("paymentOptionDescription", fullAdmissionResponse.getPaymentIntention()
                .getPaymentOption().getDescription().toLowerCase());
        }

        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        byte[] defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(defendantResponse, buildResponseFileBaseName(claim.getReferenceNumber()) + PDF);
    }
}
