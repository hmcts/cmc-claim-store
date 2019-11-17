package uk.gov.hmcts.cmc.ccd.assertion;

import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ClaimAssert extends CustomAssert<ClaimAssert, Claim> {

    ClaimAssert(Claim actual) {
        super("CCDCase", actual, ClaimAssert.class);
    }

    public ClaimAssert isEqualTo(CCDCase expected) {
        isNotNull();

        compare("previousServiceCaseReference",
            expected.getPreviousServiceCaseReference(),
            Optional.ofNullable(actual.getReferenceNumber()));

        compare("submitterId",
            expected.getSubmitterId(),
            Optional.ofNullable(actual.getSubmitterId()));

        compare("submittedOn",
            expected.getSubmittedOn(),
            Optional.ofNullable(actual.getCreatedAt()));

        compare("externalId",
            expected.getExternalId(),
            Optional.ofNullable(actual.getExternalId()));

        compare("issuedOn",
            expected.getIssuedOn(),
            Optional.ofNullable(actual.getIssuedOn()));

        compare("submitterEmail",
            expected.getSubmitterEmail(),
            Optional.ofNullable(actual.getSubmitterEmail()));

        if (expected.getTotalAmount() != null) {
            // there seems to be a problem with the test data not always defining this
            compare("totalAmount",
                expected.getTotalAmount(),
                actual.getTotalAmountTillDateOfIssue(),
                (e, a) -> assertMoney(a).isEqualTo(e));
        }

        compare("state",
            expected.getState(),
            Optional.ofNullable(actual.getState()).map(ClaimState::getValue));

        if (expected.getCurrentInterestAmount() != null) {
            // there seems to be a problem with the test data not always defining this
            compare("currentInterestAmount",
                expected.getCurrentInterestAmount(),
                actual.getTotalInterestTillDateOfIssue(),
                (e, a) -> assertMoney(a).isEqualTo(e));
        }

        ClaimData data = actual.getClaimData();
        compare("reason",
            expected.getReason(),
            Optional.ofNullable(data.getReason()));

        compare("feeCode",
            expected.getFeeCode(),
            data.getFeeCode());

        compare("feeAccountNumber",
            expected.getFeeAccountNumber(),
            data.getFeeAccountNumber());

        compare("feeAmountInPennies",
            expected.getFeeAmountInPennies(), NumberUtils::createBigInteger,
            data.getFeeAmountInPennies());

        compare("externalReferenceNumber",
            expected.getExternalReferenceNumber(),
            data.getExternalReferenceNumber());

        compare("preferredCourt",
            expected.getPreferredCourt(),
            data.getPreferredCourt());

        Amount amount = data.getAmount();
        if (amount instanceof AmountBreakDown) {
            AmountBreakDown amountBreakDown = (AmountBreakDown) amount;

            compareCollections(
                expected.getAmountBreakDown(), amountBreakDown.getRows(),
                CCDAmountRow::getReason, AmountRow::getReason,
                (ccdAmountRow, amountRow) -> {
                    compare("amountRowReason",
                        ccdAmountRow.getReason(),
                        Optional.ofNullable(amountRow.getReason()));
                    compare("amount",
                        ccdAmountRow.getAmount(),
                        Optional.ofNullable(amountRow.getAmount()),
                        (e, a) -> assertMoney(a).isEqualTo(e));
                }
            );
        } else if (amount instanceof AmountRange) {
            AmountRange amountRange = (AmountRange) amount;

            compare("amountHigherValue",
                expected.getAmountHigherValue(),
                Optional.ofNullable(amountRange.getHigherValue()),
                (e, a) -> assertMoney(a).isEqualTo(e));

            compare("amountLowerValue",
                expected.getAmountLowerValue(),
                amountRange.getLowerValue(),
                (e, a) -> assertMoney(a).isEqualTo(e));
        } else {
            org.assertj.core.api.Assertions.assertThat(amount).isInstanceOf(NotKnown.class);
        }

        Optional<Interest> actualInterest = Optional.ofNullable(data.getInterest());

        compare("interestRate",
            expected.getInterestRate(),
            actualInterest.map(Interest::getRate));

        compare("interestType",
            expected.getInterestType(), Enum::name,
            actualInterest.map(Interest::getType).map(Enum::name));

        compare("interestReason",
            expected.getInterestReason(),
            actualInterest.map(Interest::getReason));

        compare("interestSpecificDailyAmount",
            expected.getInterestSpecificDailyAmount(),
            actualInterest.flatMap(Interest::getSpecificDailyAmount),
            (e, a) -> assertMoney(a).isEqualTo(e));

        Optional<InterestDate> actualInterestDate = actualInterest.map(Interest::getInterestDate);

        compare("interestClaimStartDate",
            expected.getInterestClaimStartDate(),
            actualInterestDate.map(InterestDate::getDate));

        compare("interestDateType",
            expected.getInterestDateType(), Enum::name,
            actualInterestDate.map(InterestDate::getType).map(Enum::name));

        compare("interestStartDateReason",
            expected.getInterestStartDateReason(),
            actualInterestDate.map(InterestDate::getReason));

        compare("interestEndDateType",
            expected.getInterestEndDateType(), Enum::name,
            actualInterestDate.map(InterestDate::getEndDateType).map(Enum::name));

        Optional<Payment> actualPayment = data.getPayment();

        compare("paymentId",
            expected.getPaymentId(),
            actualPayment.map(Payment::getId));

        compare("paymentReference",
            expected.getPaymentReference(),
            actualPayment.map(Payment::getReference));

        compare("paymentAmount",
            expected.getPaymentAmount(),
            actualPayment.map(Payment::getAmount),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("paymentDateCreated",
            expected.getPaymentDateCreated(),
            actualPayment.map(Payment::getDateCreated).map(dateCreated -> LocalDate.parse(dateCreated, ISO_DATE)));

        compare("paymentStatus",
            expected.getPaymentStatus(), String::toUpperCase,
            actualPayment.map(Payment::getStatus).map(Enum::name));

        compare("personalInjuryGeneralDamages",
            expected.getPersonalInjuryGeneralDamages(),
            data.getPersonalInjury().map(PersonalInjury::getGeneralDamages).map(Enum::name));

        compare("houseDisrepairCostOfRepairDamages",
            expected.getHousingDisrepairCostOfRepairDamages(),
            data.getHousingDisrepair().map(HousingDisrepair::getCostOfRepairsDamages).map(Enum::name));

        compare("houseDisrepairOtherDamages",
            expected.getHousingDisrepairOtherDamages(),
            data.getHousingDisrepair().flatMap(HousingDisrepair::getOtherDamages).map(Enum::name));

        compare("sotSignerName",
            expected.getSotSignerName(),
            data.getStatementOfTruth().map(StatementOfTruth::getSignerName));

        compare("sotSignerRole",
            expected.getSotSignerRole(),
            data.getStatementOfTruth().map(StatementOfTruth::getSignerRole));

        compareCollections(
            expected.getApplicants(), data.getClaimants(),
            CCDApplicant::getPartyName, Party::getName,
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getRespondents(), data.getDefendants(),
            CCDRespondent::getClaimantProvidedPartyName, TheirDetails::getName,
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compare("reviewOrder",
            expected.getReviewOrder(),
            actual.getReviewOrder(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("channel",
            expected.getChannel(), Enum::name,
            actual.getChannel().map(Enum::name));

        compare("intentionToProceedDeadline",
            expected.getIntentionToProceedDeadline(),
            Optional.ofNullable(actual.getIntentionToProceedDeadline()));

        return this;
    }

}
