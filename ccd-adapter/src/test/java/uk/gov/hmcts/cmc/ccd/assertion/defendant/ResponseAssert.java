package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.assertion.Assertions;
import uk.gov.hmcts.cmc.ccd.assertion.EvidenceRowAssert;
import uk.gov.hmcts.cmc.ccd.assertion.TimelineEventAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    public ResponseAssert(Response response) {
        super(response, ResponseAssert.class);
    }

    private DefendantPartyAssert assertThat(Party party) {
        return new DefendantPartyAssert(party);
    }

    private static TimelineEventAssert assertThat(TimelineEvent timelineEvent) {
        return new TimelineEventAssert(timelineEvent);
    }

    private static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
    }

    private static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }

    private static StatementOfMeansAssert assertThat(StatementOfMeans statementOfMeans) {
        return new StatementOfMeansAssert(statementOfMeans);
    }

    public ResponseAssert isEqualTo(CCDRespondent ccdRespondent) {
        isNotNull();

        switch (actual.getResponseType()) {
            case FULL_DEFENCE:
                assertFullDefenceResponse(ccdRespondent);
                break;
            case FULL_ADMISSION:
                assertFullAdmissionResponse(ccdRespondent);
                break;
            case PART_ADMISSION:
                assertPartAdmissionResponse(ccdRespondent);
                break;
            default:
                throw new AssertionError("Invalid response type");
        }

        return this;
    }

    private void assertPartAdmissionResponse(CCDRespondent ccdRespondent) {
        assertResponse(ccdRespondent);
        PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) actual;

        String message = String.format("Expected CCDRespondent.responseAmount to be <%s> but was <%s>",
            ccdRespondent.getResponseAmount(), partAdmissionResponse.getAmount());
        assertMoney(partAdmissionResponse.getAmount()).isEqualTo(ccdRespondent.getResponseAmount(), message);

        if (!Objects.equals(partAdmissionResponse.getDefence(), ccdRespondent.getResponseDefence())) {
            failWithMessage("Expected CCDRespondent.responseDefence to be <%s> but was <%s>",
                ccdRespondent.getResponseDefence(), partAdmissionResponse.getDefence());
        }

        partAdmissionResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> assertPaymentDeclaration(ccdRespondent, paymentDeclaration));

        partAdmissionResponse.getPaymentIntention()
            .ifPresent(paymentIntention ->
                assertThat(paymentIntention).isEqualTo(ccdRespondent.getDefendantPaymentIntention()));

        if (!Objects.equals(partAdmissionResponse.getDefence(), ccdRespondent.getResponseDefence())) {
            failWithMessage("Expected CCDRespondent.responseDefence to be <%s> but was <%s>",
                ccdRespondent.getResponseDefence(), partAdmissionResponse.getDefence());
        }

        partAdmissionResponse.getTimeline().ifPresent(assertDefendantTimelineConsumer(ccdRespondent));

        partAdmissionResponse.getEvidence().ifPresent(assertDefendantEvidenceConsumer(ccdRespondent));

        partAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> assertThat(statementOfMeans).isEqualTo(ccdRespondent.getStatementOfMeans()));

        partAdmissionResponse.getDirectionsQuestionnaire().ifPresent(Assertions::assertThat);

    }

    private void assertFullAdmissionResponse(CCDRespondent ccdRespondent) {
        assertResponse(ccdRespondent);
        FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) actual;

        assertThat(fullAdmissionResponse.getPaymentIntention()).isEqualTo(ccdRespondent.getDefendantPaymentIntention());

        fullAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> assertThat(statementOfMeans).isEqualTo(ccdRespondent.getStatementOfMeans()));
    }

    private void assertFullDefenceResponse(CCDRespondent respondent) {
        assertResponse(respondent);
        FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) actual;

        if (!Objects.equals(
            fullDefenceResponse.getDefenceType().name(),
            respondent.getResponseDefenceType().name()
        )) {
            failWithMessage("Expected CCDRespondent.responseDefenceType to be <%s> but was <%s>",
                respondent.getResponseDefenceType(), fullDefenceResponse.getDefenceType());
        }

        if (!Objects.equals(fullDefenceResponse.getDefence().orElse(null), respondent.getResponseDefence())) {
            failWithMessage("Expected CCDRespondent.responseDefence to be <%s> but was <%s>",
                respondent.getResponseDefence(), fullDefenceResponse.getDefence());
        }

        fullDefenceResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> assertPaymentDeclaration(respondent, paymentDeclaration));

        fullDefenceResponse.getTimeline().ifPresent(assertDefendantTimelineConsumer(respondent));

        fullDefenceResponse.getEvidence().ifPresent(assertDefendantEvidenceConsumer(respondent));

        fullDefenceResponse.getDirectionsQuestionnaire().ifPresent(Assertions::assertThat);
    }

    private Consumer<DefendantEvidence> assertDefendantEvidenceConsumer(CCDRespondent ccdRespondent) {
        return evidence -> {
            assertEquals(evidence.getRows().size(), ccdRespondent.getResponseEvidenceRows().size());
            if (!Objects.equals(evidence.getComment().orElse(null), ccdRespondent.getResponseEvidenceComment())) {
                failWithMessage("Expected CCDRespondent.responseEvidenceComment to be <%s> but was <%s>",
                    ccdRespondent.getResponseEvidenceComment(), evidence.getComment());
            }
            evidence.getRows()
                .forEach(evidenceRow -> assertEvidenceRow(evidenceRow, ccdRespondent.getResponseEvidenceRows()));
        };
    }

    private Consumer<DefendantTimeline> assertDefendantTimelineConsumer(CCDRespondent ccdRespondent) {
        return defendantTimeline -> {
            assertEquals(defendantTimeline.getEvents().size(), ccdRespondent.getDefendantTimeLineEvents().size());
            if (!Objects.equals(
                defendantTimeline.getComment().orElse(null),
                ccdRespondent.getDefendantTimeLineComment())
            ) {
                failWithMessage("Expected CCDRespondent.defendantTimeLineComment to be <%s> but was <%s>",
                    ccdRespondent.getDefendantTimeLineComment(), defendantTimeline.getComment());
            }
            defendantTimeline.getEvents()
                .forEach(event -> assertTimelineEvent(event, ccdRespondent.getDefendantTimeLineEvents()));
        };
    }

    private void assertPaymentDeclaration(CCDRespondent ccdRespondent, PaymentDeclaration paymentDeclaration) {
        paymentDeclaration.getPaidAmount().ifPresent(paidAmount ->
            assertMoney(paidAmount).isEqualTo(ccdRespondent.getPaymentDeclarationPaidAmount())
        );

        if (!Objects.equals(paymentDeclaration.getPaidDate(), ccdRespondent.getPaymentDeclarationPaidDate())) {
            failWithMessage("Expected CCDRespondent.paymentDeclarationPaidDate to be <%s> but was <%s>",
                ccdRespondent.getPaymentDeclarationPaidDate(), paymentDeclaration.getPaidDate());
        }

        if (!Objects.equals(paymentDeclaration.getExplanation(), ccdRespondent.getPaymentDeclarationExplanation())) {
            failWithMessage("Expected CCDRespondent.paymentDeclarationExplanation to be <%s> but was <%s>",
                ccdRespondent.getPaymentDeclarationExplanation(), paymentDeclaration.getExplanation());
        }
    }

    private void assertEvidenceRow(EvidenceRow actualEvidenceRow,
                                   List<CCDCollectionElement<CCDEvidenceRow>> ccdEvidences) {

        ccdEvidences.stream()
            .map(CCDCollectionElement::getValue)
            .filter(evidenceRow -> actualEvidenceRow.getType().name().equals(evidenceRow.getType().name()))
            .findFirst()
            .ifPresent(evidenceRow -> assertThat(actualEvidenceRow).isEqualTo(evidenceRow));
    }

    private void assertTimelineEvent(TimelineEvent actualEvent,
                                     List<CCDCollectionElement<CCDTimelineEvent>> ccdTimeline) {

        ccdTimeline.stream()
            .map(CCDCollectionElement::getValue)
            .filter(timelineEvent -> actualEvent.getDate().equals(timelineEvent.getDate()))
            .findFirst()
            .ifPresent(event -> assertThat(actualEvent).isEqualTo(event));
    }

    private void assertResponse(CCDRespondent ccdRespondent) {
        if (!Objects.equals(actual.getResponseType().name(), ccdRespondent.getResponseType().name())) {
            failWithMessage("Expected CCDRespondent.responseType to be <%s> but was <%s>",
                ccdRespondent.getResponseType(), actual.getResponseType());
        }

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(
                actual.getFreeMediation().orElse(YesNoOption.YES).name(),
                ccdRespondent.getResponseFreeMediationOption().name())
            ) {
                failWithMessage(
                    "Expected CCDRespondent.responseFreeMediationOption to be <%s> but was <%s>",
                    ccdRespondent.getResponseFreeMediationOption(), actual.getFreeMediation()
                );
            }
        });

        actual.getMediationPhoneNumber().ifPresent(mediationPhoneNumber -> {
            if (!Objects.equals(
                mediationPhoneNumber,
                ccdRespondent.getResponseMediationPhoneNumber().getTelephoneNumber())) {
                failWithMessage("Expected CCDDefendant.responseMediationPhoneNumber to be "
                        + "<%s> but was <%s>",
                    ccdRespondent.getResponseMediationPhoneNumber(),
                    actual.getMediationPhoneNumber());
            }
        });

        actual.getMediationContactPerson().ifPresent(mediationContactPerson -> {
            if (!Objects.equals(
                mediationContactPerson,
                ccdRespondent.getResponseMediationContactPerson())) {
                failWithMessage("Expected CCDDefendant.responseMediationContactPerson to be "
                        + "<%s> but was <%s>",
                    ccdRespondent.getResponseMediationContactPerson(),
                    actual.getMediationContactPerson());
            }
        });

        if (!Objects.equals(actual.getMoreTimeNeeded().name(),
            ccdRespondent.getResponseMoreTimeNeededOption().name())) {
            failWithMessage("Expected CCDRespondent.responseMoreTimeNeededOption to be <%s> but was <%s>",
                ccdRespondent.getResponseMoreTimeNeededOption(), actual.getMoreTimeNeeded());
        }

        actual.getStatementOfTruth()
            .ifPresent(statementOfTruth -> assertStatementOfTruth(ccdRespondent, statementOfTruth));

        assertThat(actual.getDefendant()).isEqualTo(ccdRespondent);
    }

    private void assertStatementOfTruth(CCDRespondent ccdRespondent, StatementOfTruth statementOfTruth) {
        if (!Objects.equals(statementOfTruth.getSignerName(), ccdRespondent.getResponseDefendantSOTSignerName())) {
            failWithMessage(
                "Expected CCDRespondent.responseDefendantSOTSignerName to be <%s> but was <%s>",
                statementOfTruth.getSignerName(), ccdRespondent.getResponseDefendantSOTSignerName());
        }

        if (!Objects.equals(statementOfTruth.getSignerRole(), ccdRespondent.getResponseDefendantSOTSignerRole())) {
            failWithMessage(
                "Expected CCDRespondent.responseDefendantSOTSignerRole to be <%s> but was <%s>",
                statementOfTruth.getSignerRole(), ccdRespondent.getResponseDefendantSOTSignerRole());
        }
    }
}
