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
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class DefendantResponseStaffNotificationService {

    public static final String FILE_NAME_FORMAT = "%s-claim-response.pdf";

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

        Map<String, Object> input = wrapInMap(claim, defendantEmail);
        EmailContent emailContent = isFullAdmission(claim)
            ? fullAdmissionStaffEmailContentProvider.createContent(input)
            : fullDefenceStaffEmailContentProvider.createContent(input);

        byte[] defendantResponse = defendantResponseReceiptService.createPdf(claim);
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(pdf(
                    defendantResponse,
                    format(FILE_NAME_FORMAT, claim.getReferenceNumber())
                ))
            )
        );
    }

    private boolean isFullAdmission(Claim claim) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalArgumentException::new).getResponseType();
        return responseType == ResponseType.FULL_ADMISSION;
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

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                map.put("defendantMobilePhone", claim.getResponse()
                    .orElseThrow(IllegalStateException::new)
                    .getDefendant()
                    .getMobilePhone()
                    .orElse(null));
                break;
            case FULL_ADMISSION:
                FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
                map.put("paymentOption", fullAdmissionResponse.getPaymentOption());
                map.put("paymentOptionDescription", fullAdmissionResponse.getPaymentOption().getDescription());
                map.put("responseDeadline", formatDate(claim.getResponseDeadline()));
                map.put("FourteenDaysFromNow", formatDate(now().plusDays(14)));
                break;
            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
        return map;
    }

}
