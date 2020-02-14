package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.assertion.DirectionsQuestionnaireAssert;
import uk.gov.hmcts.cmc.ccd.assertion.EvidenceRowAssert;
import uk.gov.hmcts.cmc.ccd.assertion.TimelineEventAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
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
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ResponseAssert extends CustomAssert<ResponseAssert, Response> {

    public ResponseAssert(Response response) {
        super("Response", response, ResponseAssert.class);
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

    private static DirectionsQuestionnaireAssert assertThat(DirectionsQuestionnaire directionsQuestionnaire) {
        return new DirectionsQuestionnaireAssert(directionsQuestionnaire);
    }

    public ResponseAssert isEqualTo(CCDRespondent expected) {
        isNotNull();

        switch (actual.getResponseType()) {
            case FULL_DEFENCE:
                assertFullDefenceResponse(expected);
                break;
            case FULL_ADMISSION:
                assertFullAdmissionResponse(expected);
                break;
            case PART_ADMISSION:
                assertPartAdmissionResponse(expected);
                break;
            default:
                throw new AssertionError("Invalid response type");
        }

        return this;
    }

    private void assertPartAdmissionResponse(CCDRespondent expected) {
        assertResponse(expected);
        PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) actual;

        compare("amount",
            expected.getResponseAmount(),
            Optional.ofNullable(partAdmissionResponse.getAmount()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("defence",
            expected.getResponseDefence(),
            Optional.ofNullable(partAdmissionResponse.getDefence()));

        assertPaymentDeclaration(expected, partAdmissionResponse.getPaymentDeclaration().orElse(null));

        compare("paymentIntention",
            expected.getDefendantPaymentIntention(),
            partAdmissionResponse.getPaymentIntention(),
            (e, a) -> assertThat(a).isEqualTo(e));

        assertDefendantTimeline(expected, partAdmissionResponse.getTimeline().orElse(null));

        assertDefendantEvidence(expected, partAdmissionResponse.getEvidence().orElse(null));

        compare("statementOfMeans",
            expected.getStatementOfMeans(),
            partAdmissionResponse.getStatementOfMeans(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("directionsQuestionnaire",
            expected.getDirectionsQuestionnaire(),
            partAdmissionResponse.getDirectionsQuestionnaire(),
            (e, a) -> assertThat(a).isEqualTo(e));
    }

    private void assertFullAdmissionResponse(CCDRespondent expected) {
        assertResponse(expected);
        FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) actual;

        compare("paymentIntention",
            expected.getDefendantPaymentIntention(),
            Optional.ofNullable(fullAdmissionResponse.getPaymentIntention()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("statementOfMeans",
            expected.getStatementOfMeans(),
            fullAdmissionResponse.getStatementOfMeans(),
            (e, a) -> assertThat(a).isEqualTo(e));
    }

    private void assertFullDefenceResponse(CCDRespondent expected) {
        assertResponse(expected);
        FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) actual;

        compare("defenceType",
            expected.getResponseDefenceType(), Enum::name,
            Optional.ofNullable(fullDefenceResponse.getDefenceType()).map(Enum::name));

        compare("defence",
            expected.getResponseDefence(),
            fullDefenceResponse.getDefence());

        assertPaymentDeclaration(expected, fullDefenceResponse.getPaymentDeclaration().orElse(null));

        assertDefendantTimeline(expected, fullDefenceResponse.getTimeline().orElse(null));

        assertDefendantEvidence(expected, fullDefenceResponse.getEvidence().orElse(null));

        compare("directionsQuestionnaire",
            expected.getDirectionsQuestionnaire(),
            fullDefenceResponse.getDirectionsQuestionnaire(),
            (e, a) -> assertThat(a).isEqualTo(e));
    }

    private void assertDefendantEvidence(CCDRespondent expected, DefendantEvidence actualEvidence) {
        if (actualEvidence == null) {
            if ((expected.getResponseEvidenceRows() != null && !expected.getResponseEvidenceRows().isEmpty())
                || expected.getResponseEvidenceComment() != null) {

                failExpectedPresent("evidence", ImmutableMap.of(
                    "evidenceRows", expected.getResponseEvidenceRows() == null
                        ? "null" : expected.getResponseEvidenceRows(),
                    "comment", expected.getResponseEvidenceComment()
                ));
            }
            return;
        }

        compareCollections(
            expected.getResponseEvidenceRows(), actualEvidence.getRows(),
            row -> row.getType().name(), row -> row.getType().name(),
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compare("evidence.comment",
            expected.getResponseEvidenceComment(),
            actualEvidence.getComment());
    }

    private void assertDefendantTimeline(CCDRespondent expected, DefendantTimeline actualTimeline) {
        if (actualTimeline == null) {
            if ((expected.getDefendantTimeLineEvents() != null && !expected.getDefendantTimeLineEvents().isEmpty())
                || expected.getDefendantTimeLineComment() != null) {

                failExpectedPresent("timeline", ImmutableMap.of(
                    "events", expected.getDefendantTimeLineEvents() == null
                        ? "null" : expected.getDefendantTimeLineEvents(),
                    "comment", expected.getDefendantTimeLineComment()
                ));
            }
            return;
        }

        compareCollections(
            expected.getDefendantTimeLineEvents(), actualTimeline.getEvents(),
            CCDTimelineEvent::getDate, TimelineEvent::getDate,
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compare("timeline.comment",
            expected.getDefendantTimeLineComment(),
            actualTimeline.getComment());
    }

    private void assertPaymentDeclaration(CCDRespondent expected, PaymentDeclaration actualDeclaration) {
        if (actualDeclaration == null) {
            if (expected.hasPaymentDeclaration()) {
                failExpectedPresent("paymentDeclaration", ImmutableMap.of(
                    "paidDate", expected.getPaymentDeclarationPaidDate(),
                    "paidAmount", expected.getPaymentDeclarationPaidAmount(),
                    "explanation", expected.getPaymentDeclarationExplanation()
                ));
            }
            return;
        }

        if (!expected.hasPaymentDeclaration()) {
            failExpectedAbsent("paymentDeclaration", actualDeclaration);
        }

        compare("paymentDeclaration.paidAmount",
            expected.getPaymentDeclarationPaidAmount(),
            actualDeclaration.getPaidAmount(),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("paymentDeclaration.paidDate",
            expected.getPaymentDeclarationPaidDate(),
            Optional.ofNullable(actualDeclaration.getPaidDate()));

        compare("paymentDeclaration.explanation",
            expected.getPaymentDeclarationExplanation(),
            Optional.ofNullable(actualDeclaration.getExplanation()));
    }

    private void assertResponse(CCDRespondent expected) {
        compare("responseType",
            expected.getResponseType(), Enum::name,
            Optional.ofNullable(actual.getResponseType()).map(Enum::name));

        compare("freeMediation",
            expected.getResponseFreeMediationOption(), Enum::name,
            actual.getFreeMediation().map(Enum::name));

        compare("mediationPhoneNumber",
            expected.getResponseMediationPhoneNumber(), CCDTelephone::getTelephoneNumber,
            actual.getMediationPhoneNumber());

        compare("mediationContactPerson",
            expected.getResponseMediationContactPerson(),
            actual.getMediationContactPerson());

        compare("moreTimeNeeded",
            expected.getResponseMoreTimeNeededOption(), Enum::name,
            Optional.ofNullable(actual.getMoreTimeNeeded()).map(Enum::name));

        assertStatementOfTruth(expected, actual.getStatementOfTruth().orElse(null));

        assertThat(actual.getDefendant()).isEqualTo(expected);
    }

    private void assertStatementOfTruth(CCDRespondent expected, StatementOfTruth actualSoT) {
        if (actualSoT == null) {
            if (expected.hasStatementOfTruth()) {
                failExpectedPresent("statementOfTruth", ImmutableMap.of(
                    "signerName", expected.getResponseDefendantSOTSignerName(),
                    "signerRole", expected.getResponseDefendantSOTSignerRole()
                ));
            }
            return;
        }

        if (!expected.hasStatementOfTruth()) {
            failExpectedAbsent("statementOfTruth", actualSoT);
        }

        compare("statementOfTruth.signerName",
            expected.getResponseDefendantSOTSignerName(),
            Optional.ofNullable(actualSoT.getSignerName()));

        compare("statementOfTruth.signerRole",
            expected.getResponseDefendantSOTSignerRole(),
            Optional.ofNullable(actualSoT.getSignerRole()));
    }
}
