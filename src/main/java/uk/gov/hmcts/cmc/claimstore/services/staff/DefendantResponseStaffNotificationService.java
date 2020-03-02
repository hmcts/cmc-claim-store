package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantAdmissionStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.FullDefenceStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.PartyUtils.isCompanyOrOrganisation;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class DefendantResponseStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final FullDefenceStaffEmailContentProvider fullDefenceStaffEmailContentProvider;
    private final DefendantAdmissionStaffEmailContentProvider defendantAdmissionStaffEmailContentProvider;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefendantResponseStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        FullDefenceStaffEmailContentProvider fullDefenceStaffEmailContentProvider,
        DefendantAdmissionStaffEmailContentProvider defendantAdmissionStaffEmailContentProvider,
        DefendantResponseReceiptService defendantResponseReceiptService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.fullDefenceStaffEmailContentProvider = fullDefenceStaffEmailContentProvider;
        this.defendantAdmissionStaffEmailContentProvider = defendantAdmissionStaffEmailContentProvider;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    public void notifyStaffDefenceSubmittedFor(
        Claim claim,
        String defendantEmail
    ) {
        ResponseType responseType = claim.getResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_RESPONSE))
            .getResponseType();
        EmailContent emailContent;

        emailContent = isFullAdmission(responseType) || isPartAdmission(responseType)
            ? defendantAdmissionStaffEmailContentProvider.createContent(wrapInMap(claim, defendantEmail))
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

    private static boolean isPartAdmission(ResponseType responseType) {
        return responseType == PART_ADMISSION;
    }

    private static boolean isFullAdmission(ResponseType responseType) {
        return responseType == FULL_ADMISSION;
    }

    public static Map<String, Object> wrapInMap(
        Claim claim,
        String defendantEmail
    ) {
        Map<String, Object> map = new HashMap<>();

        Response response = claim.getResponse()
            .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE));
        map.put("claim", claim);
        map.put("response", response);
        map.put("defendantEmail", defendantEmail);
        map.put("defendantPhone", response
            .getDefendant()
            .getPhone()
            .orElse(null));
        map.put("isCompanyOrOrganisation", isCompanyOrOrganisation(response.getDefendant()));

        if (isFullAdmission(response.getResponseType())) {
            FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
            map.put("responseType", "full admission");
            map.put("admissionPaymentIntention", fullAdmissionResponse.getPaymentIntention() != null);
            map.put("paymentOptionDescription", fullAdmissionResponse.getPaymentIntention()
                .getPaymentOption().getDescription().toLowerCase());
        }

        if (isPartAdmission(response.getResponseType())) {
            PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;

            map.put("responseType", "partial admission");
            Optional<PaymentIntention> responsePaymentIntention = partAdmissionResponse.getPaymentIntention();
            map.put("admissionPaymentIntention", responsePaymentIntention.isPresent());
            responsePaymentIntention.ifPresent(paymentIntention ->
                map.put("paymentOptionDescription", paymentIntention.getPaymentOption()
                    .getDescription().toLowerCase()));
        }

        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        PDF defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(
            defendantResponse.getBytes(),
            defendantResponse.getFilename());
    }
}
