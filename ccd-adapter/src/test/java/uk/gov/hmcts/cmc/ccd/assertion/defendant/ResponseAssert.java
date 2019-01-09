package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.assertion.EvidenceRowAssert;
import uk.gov.hmcts.cmc.ccd.assertion.TimelineEventAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
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

    public ResponseAssert isEqualTo(CCDDefendant ccdDefendant) {
        isNotNull();

        switch (actual.getResponseType()) {
            case FULL_DEFENCE:
                assertFullDefenceResponse(ccdDefendant);
                break;
            case FULL_ADMISSION:
                assertFullAdmissionResponse(ccdDefendant);
                break;
            case PART_ADMISSION:
                assertPartAdmissionResponse(ccdDefendant);
                break;
            default:
                throw new AssertionError("Invalid response type");
        }

        return this;
    }

    private void assertPartAdmissionResponse(CCDDefendant ccdDefendant) {
        assertResponse(ccdDefendant);
        PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) actual;

        if (!Objects.equals(partAdmissionResponse.getAmount(), ccdDefendant.getResponseAmount())) {
            failWithMessage("Expected CCDDefendant.responseAmount to be <%s> but was <%s>",
                ccdDefendant.getResponseAmount(), partAdmissionResponse.getAmount());
        }

        if (!Objects.equals(partAdmissionResponse.getDefence(), ccdDefendant.getResponseDefence())) {
            failWithMessage("Expected CCDDefendant.responseDefence to be <%s> but was <%s>",
                ccdDefendant.getResponseDefence(), partAdmissionResponse.getDefence());
        }

        partAdmissionResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> assertPaymentDeclaration(ccdDefendant, paymentDeclaration));

        partAdmissionResponse.getPaymentIntention()
            .ifPresent(paymentIntention ->
                assertThat(paymentIntention).isEqualTo(ccdDefendant.getDefendantPaymentIntention()));

        if (!Objects.equals(partAdmissionResponse.getDefence(), ccdDefendant.getResponseDefence())) {
            failWithMessage("Expected CCDDefendant.responseDefence to be <%s> but was <%s>",
                ccdDefendant.getResponseDefence(), partAdmissionResponse.getDefence());
        }

        partAdmissionResponse.getTimeline().ifPresent(assertDefendantTimelineConsumer(ccdDefendant));

        partAdmissionResponse.getEvidence().ifPresent(assertDefendantEvidenceConsumer(ccdDefendant));

        partAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> assertThat(statementOfMeans).isEqualTo(ccdDefendant.getStatementOfMeans()));
    }

    private void assertFullAdmissionResponse(CCDDefendant ccdDefendant) {
        assertResponse(ccdDefendant);
        FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) actual;

        assertThat(fullAdmissionResponse.getPaymentIntention()).isEqualTo(ccdDefendant.getDefendantPaymentIntention());

        fullAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> assertThat(statementOfMeans).isEqualTo(ccdDefendant.getStatementOfMeans()));
    }

    private void assertFullDefenceResponse(CCDDefendant ccdDefendant) {
        assertResponse(ccdDefendant);
        FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) actual;

        if (!Objects.equals(
            fullDefenceResponse.getDefenceType().name(),
            ccdDefendant.getResponseDefenceType().name()
        )) {
            failWithMessage("Expected CCDDefendant.responseDefenceType to be <%s> but was <%s>",
                ccdDefendant.getResponseDefenceType(), fullDefenceResponse.getDefenceType());
        }

        if (!Objects.equals(fullDefenceResponse.getDefence().orElse(null), ccdDefendant.getResponseDefence())) {
            failWithMessage("Expected CCDDefendant.responseDefence to be <%s> but was <%s>",
                ccdDefendant.getResponseDefence(), fullDefenceResponse.getDefence());
        }

        fullDefenceResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> assertPaymentDeclaration(ccdDefendant, paymentDeclaration));

        fullDefenceResponse.getTimeline().ifPresent(assertDefendantTimelineConsumer(ccdDefendant));

        fullDefenceResponse.getEvidence().ifPresent(assertDefendantEvidenceConsumer(ccdDefendant));
    }

    private Consumer<DefendantEvidence> assertDefendantEvidenceConsumer(CCDDefendant ccdDefendant) {
        return evidence -> {
            assertEquals(evidence.getRows().size(), ccdDefendant.getResponseEvidenceRows().size());
            if (!Objects.equals(evidence.getComment().orElse(null), ccdDefendant.getResponseEvidenceComment())) {
                failWithMessage("Expected CCDDefendant.responseEvidenceComment to be <%s> but was <%s>",
                    ccdDefendant.getResponseEvidenceComment(), evidence.getComment());
            }
            evidence.getRows()
                .forEach(evidenceRow -> assertEvidenceRow(evidenceRow, ccdDefendant.getResponseEvidenceRows()));
        };
    }

    private Consumer<DefendantTimeline> assertDefendantTimelineConsumer(CCDDefendant ccdDefendant) {
        return defendantTimeline -> {
            assertEquals(defendantTimeline.getEvents().size(), ccdDefendant.getDefendantTimeLineEvents().size());
            if (!Objects.equals(
                defendantTimeline.getComment().orElse(null),
                ccdDefendant.getDefendantTimeLineComment())
            ) {
                failWithMessage("Expected CCDDefendant.defendantTimeLineComment to be <%s> but was <%s>",
                    ccdDefendant.getDefendantTimeLineComment(), defendantTimeline.getComment());
            }
            defendantTimeline.getEvents()
                .forEach(event -> assertTimelineEvent(event, ccdDefendant.getDefendantTimeLineEvents()));
        };
    }

    private void assertPaymentDeclaration(CCDDefendant ccdDefendant, PaymentDeclaration paymentDeclaration) {
        if (!Objects.equals(paymentDeclaration.getPaidDate(), ccdDefendant.getPaymentDeclarationPaidDate())) {
            failWithMessage("Expected CCDDefendant.paymentDeclarationPaidDate to be <%s> but was <%s>",
                ccdDefendant.getPaymentDeclarationPaidDate(), paymentDeclaration.getPaidDate());
        }

        if (!Objects.equals(paymentDeclaration.getExplanation(), ccdDefendant.getPaymentDeclarationExplanation())) {
            failWithMessage("Expected CCDDefendant.paymentDeclarationExplanation to be <%s> but was <%s>",
                ccdDefendant.getPaymentDeclarationExplanation(), paymentDeclaration.getExplanation());
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

    private void assertResponse(CCDDefendant ccdDefendant) {
        if (!Objects.equals(actual.getResponseType().name(), ccdDefendant.getResponseType().name())) {
            failWithMessage("Expected CCDDefendant.responseType to be <%s> but was <%s>",
                ccdDefendant.getResponseType(), actual.getResponseType());
        }

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(
                actual.getFreeMediation().orElse(YesNoOption.NO).name(),
                ccdDefendant.getResponseFreeMediationOption().name())
            ) {
                failWithMessage("Expected CCDDefendant.responseFreeMediationOption to be <%s> but was <%s>",
                    ccdDefendant.getResponseFreeMediationOption(), actual.getFreeMediation());
            }
        });

        if (!Objects.equals(actual.getMoreTimeNeeded().name(), ccdDefendant.getResponseMoreTimeNeededOption().name())) {
            failWithMessage("Expected CCDDefendant.responseMoreTimeNeededOption to be <%s> but was <%s>",
                ccdDefendant.getResponseMoreTimeNeededOption(), actual.getMoreTimeNeeded());
        }

        actual.getStatementOfTruth()
            .ifPresent(statementOfTruth -> assertStatementOfTruth(ccdDefendant, statementOfTruth));

        assertThat(actual.getDefendant()).isEqualTo(ccdDefendant);
    }

    private void assertStatementOfTruth(CCDDefendant ccdDefendant, StatementOfTruth statementOfTruth) {
        if (!Objects.equals(statementOfTruth.getSignerName(), ccdDefendant.getResponseDefendantSOTSignerName())) {
            failWithMessage(
                "Expected CCDDefendant.responseDefendantSOTSignerName to be <%s> but was <%s>",
                statementOfTruth.getSignerName(), ccdDefendant.getResponseDefendantSOTSignerName());
        }

        if (!Objects.equals(statementOfTruth.getSignerRole(), ccdDefendant.getResponseDefendantSOTSignerRole())) {
            failWithMessage(
                "Expected CCDDefendant.responseDefendantSOTSignerRole to be <%s> but was <%s>",
                statementOfTruth.getSignerRole(), ccdDefendant.getResponseDefendantSOTSignerRole());
        }
    }
}
