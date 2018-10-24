package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public abstract class BaseCCJPdfTest extends BasePdfTest {

    protected void assertHeaderDetails(Claim caseGiven, String pdfAsText, String expectedTitle) {
        assertThat(pdfAsText).contains(expectedTitle);
        assertThat(pdfAsText).contains("In the County Court Business Centre");
        assertThat(pdfAsText).contains("Online Civil Money Claims");
        assertThat(pdfAsText).contains("Claim number: " + caseGiven.getReferenceNumber());
        assertThat(pdfAsText).contains("Requested: " + formatDate(caseGiven.getCountyCourtJudgmentRequestedAt()));
    }

    protected void assertPartyAndContactDetails(ClaimData claimData, String pdfAsText) {
        assertThat(pdfAsText).contains("Claimant details");
        assertThat(pdfAsText).contains("Name: " + claimData.getClaimant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(claimData.getClaimant().getAddress()));
        claimData.getClaimant().getMobilePhone()
            .ifPresent(value -> assertThat(pdfAsText).contains("Telephone: " + value));
        assertThat(pdfAsText).contains("Defendant details");
        assertThat(pdfAsText).contains("Name: " + claimData.getDefendant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(claimData.getDefendant().getAddress()));
        claimData.getDefendant().getEmail().ifPresent(value -> assertThat(pdfAsText).contains("Email:"));
    }

    protected void assertClaimAmountDetails(Claim caseGiven, String pdfAsText) {
        ClaimData claimData = caseGiven.getClaimData();
        assertThat(pdfAsText).contains("Claim amount details");
        assertThat(pdfAsText).contains("Claim amount: "
            + Formatting.formatMoney(((AmountBreakDown) claimData.getAmount()).getTotalAmount()));
        assertInterest(claimData, pdfAsText);
        CountyCourtJudgment countyCourtJudgment = caseGiven.getCountyCourtJudgment();
        PaymentOption paymentOption = countyCourtJudgment.getPaymentOption();
        switch (paymentOption) {
            case IMMEDIATELY:
                assertThat(pdfAsText).contains("When you want the \n"
                    + "defendant to pay: " + paymentOption.getDescription());
                break;
            case INSTALMENTS:
            case BY_SPECIFIED_DATE:
                assertThat(pdfAsText).contains("How you want the \n"
                    + "defendant to pay: " + paymentOption.getDescription());
                break;
            default:
                throw new IllegalArgumentException("Wrong payment option: " + paymentOption.name());

        }
    }

    private void assertInterest(ClaimData claimData, String pdfAsText) {
        Interest.InterestType interestType = claimData.getInterest().getType();
        switch (interestType) {
            case NO_INTEREST:
                assertThat(pdfAsText.contains("Interest: No interest claimed"));
                break;
            case STANDARD:
            case DIFFERENT:
                assertThat(pdfAsText.contains("Interest rate claimed:" + claimData.getInterest().getRate()));
                break;
            case BREAKDOWN:
                assertThat(pdfAsText.contains("Total interest amount:" + claimData
                    .getInterest()
                    .getInterestBreakdown()
                    .getTotalAmount()));
                break;
            default:
                throw new IllegalArgumentException("Incorrect interest type: " + interestType);
        }
    }

    protected void assertTotalAmount(Claim caseGiven, String pdfAsText, AmountContent amountContent) {
        ClaimData claimData = caseGiven.getClaimData();
        if (ResponseHelper.admissionResponse(caseGiven)) {
            assertThat(pdfAsText).contains("Judgment amount");
        } else {
            assertThat(pdfAsText).contains("Total amount");
        }
        assertThat(pdfAsText).contains("Claim amount: "
            + Formatting.formatMoney(((AmountBreakDown) claimData.getAmount()).getTotalAmount()));
        assertInterest(claimData, pdfAsText);
        assertThat(pdfAsText).contains("Claim fee: " + amountContent.getFeeAmount());
        assertThat(pdfAsText).contains("Subtotal: " + amountContent.getSubTotalAmount());
        assertThat(pdfAsText).contains("Amount already paid by \n"
            + "defendant: " + amountContent.getPaidAmount());
        assertThat(pdfAsText).contains("Total: " + amountContent.getRemainingAmount());

    }

    protected void assertDeclaration(Claim caseGiven, String pdfAsText) {
        assertThat(pdfAsText).contains("Declaration");
        assertThat(pdfAsText).contains("I declare that the details I have given are true to the \n"
            + "best of my knowledge.");
        assertThat(pdfAsText).contains(caseGiven.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains(formatDate(caseGiven.getCountyCourtJudgmentRequestedAt()));
    }
}
