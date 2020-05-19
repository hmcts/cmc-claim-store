package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.DirectionsQuestionnaireMapper;
import uk.gov.hmcts.cmc.ccd.mapper.EvidenceRowMapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TelephoneMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TimelineEventMapper;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseMethod;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class ResponseMapper {
    private final EvidenceRowMapper evidenceRowMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final DefendantPartyMapper defendantPartyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;
    private final TelephoneMapper telephoneMapper;
    private final MoneyMapper moneyMapper;
    private final DirectionsQuestionnaireMapper directionsQuestionnaireMapper;

    public ResponseMapper(
        EvidenceRowMapper evidenceRowMapper,
        TimelineEventMapper timelineEventMapper,
        DefendantPartyMapper defendantPartyMapper,
        PaymentIntentionMapper paymentIntentionMapper,
        StatementOfMeansMapper statementOfMeansMapper,
        TelephoneMapper telephoneMapper,
        MoneyMapper moneyMapper,
        DirectionsQuestionnaireMapper directionsQuestionnaireMapper
    ) {
        this.evidenceRowMapper = evidenceRowMapper;
        this.timelineEventMapper = timelineEventMapper;
        this.defendantPartyMapper = defendantPartyMapper;
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.statementOfMeansMapper = statementOfMeansMapper;
        this.telephoneMapper = telephoneMapper;
        this.moneyMapper = moneyMapper;
        this.directionsQuestionnaireMapper = directionsQuestionnaireMapper;
    }

    public void to(
        CCDRespondent.CCDRespondentBuilder builder,
        Response response,
        CCDParty.CCDPartyBuilder partyDetail
    ) {
        requireNonNull(builder, "builder must not be null");
        requireNonNull(response, "response must not be null");

        builder.responseType(
            CCDResponseType.valueOf(response.getResponseType().name())
        );

        builder.responseMethod(
            CCDResponseMethod.valueOf(response.getResponseMethod().name())
        );

        response.getFreeMediation().ifPresent(freeMediation ->
            builder.responseFreeMediationOption(CCDYesNoOption.valueOf(freeMediation.name())));

        response.getMediationPhoneNumber().ifPresent(phoneNo ->
            builder.responseMediationPhoneNumber(telephoneMapper.to(phoneNo)));
        response.getMediationContactPerson().ifPresent(builder::responseMediationContactPerson);

        if (response.getMoreTimeNeeded() != null) {
            builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(response.getMoreTimeNeeded().name()));
        }

        response.getStatementOfTruth().ifPresent(
            statementOfTruth -> {
                builder.responseDefendantSOTSignerName(statementOfTruth.getSignerName());
                builder.responseDefendantSOTSignerRole(statementOfTruth.getSignerRole());
            }
        );
        defendantPartyMapper.to(builder, response.getDefendant(), partyDetail);

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                toFullDefenceResponse(builder, (FullDefenceResponse) response);
                break;
            case FULL_ADMISSION:
                toFullAdmissionResponse(builder, (FullAdmissionResponse) response);
                break;
            case PART_ADMISSION:
                toPartAdmissionResponse(builder, (PartAdmissionResponse) response);
                break;
            default:
                throw new MappingException("Invalid response type " + response.getResponseType());
        }
    }

    public void from(Claim.ClaimBuilder claimBuilder, CCDCollectionElement<CCDRespondent> respondentElement) {
        CCDRespondent respondent = respondentElement.getValue();
        requireNonNull(claimBuilder, "claimBuilder must not be null");
        requireNonNull(respondent, "respondent must not be null");
        if (respondent.getResponseType() == null) {
            return;
        }

        switch (respondent.getResponseType()) {
            case FULL_DEFENCE:
                claimBuilder.response(extractFullDefence(respondentElement));
                break;
            case FULL_ADMISSION:
                claimBuilder.response(extractFullAdmission(respondentElement));
                break;
            case PART_ADMISSION:
                claimBuilder.response(extractPartAdmission(respondentElement));
                break;
            default:
                throw new MappingException("Invalid responseType");
        }
    }

    private void toPartAdmissionResponse(CCDRespondent.CCDRespondentBuilder builder, PartAdmissionResponse response) {

        builder.responseAmount(moneyMapper.to(response.getAmount()));
        response.getPaymentDeclaration().ifPresent(
            mapPaymentDeclaration(builder)
        );
        builder.responseDefence(response.getDefence());
        response.getEvidence().ifPresent(mapDefendantEvidence(builder));
        response.getTimeline().ifPresent(mapDefendantTimeline(builder));

        response.getPaymentIntention()
            .map(paymentIntentionMapper::to)
            .ifPresent(builder::defendantPaymentIntention);

        response.getStatementOfMeans()
            .map(statementOfMeansMapper::to)
            .ifPresent(builder::statementOfMeans);

        response.getDirectionsQuestionnaire()
            .map(directionsQuestionnaireMapper::to)
            .ifPresent(builder::directionsQuestionnaire);
    }

    private void toFullAdmissionResponse(CCDRespondent.CCDRespondentBuilder builder, FullAdmissionResponse response) {

        builder.defendantPaymentIntention(paymentIntentionMapper.to(response.getPaymentIntention()));

        response.getStatementOfMeans().ifPresent(
            statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans))
        );
    }

    private void toFullDefenceResponse(
        CCDRespondent.CCDRespondentBuilder builder,
        FullDefenceResponse fullDefenceResponse
    ) {
        builder.responseDefenceType(CCDDefenceType.valueOf(fullDefenceResponse.getDefenceType().name()));
        fullDefenceResponse.getDefence().ifPresent(builder::responseDefence);
        fullDefenceResponse.getPaymentDeclaration().ifPresent(mapPaymentDeclaration(builder));
        fullDefenceResponse.getEvidence().ifPresent(mapDefendantEvidence(builder));
        fullDefenceResponse.getTimeline().ifPresent(mapDefendantTimeline(builder));

        fullDefenceResponse.getDirectionsQuestionnaire()
            .map(directionsQuestionnaireMapper::to)
            .ifPresent(builder::directionsQuestionnaire);
    }

    private Consumer<DefendantTimeline> mapDefendantTimeline(CCDRespondent.CCDRespondentBuilder builder) {
        return timeline -> {
            timeline.getComment().ifPresent(builder::defendantTimeLineComment);
            builder.defendantTimeLineEvents(asStream(timeline.getEvents())
                .map(timelineEventMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            );
        };
    }

    private Consumer<DefendantEvidence> mapDefendantEvidence(CCDRespondent.CCDRespondentBuilder builder) {
        return evidence -> {
            evidence.getComment().ifPresent(builder::responseEvidenceComment);
            builder.responseEvidenceRows(asStream(evidence.getRows())
                .map(evidenceRowMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            );
        };
    }

    private Consumer<PaymentDeclaration> mapPaymentDeclaration(CCDRespondent.CCDRespondentBuilder builder) {
        return paymentDeclaration -> {
            builder.paymentDeclarationPaidDate(paymentDeclaration.getPaidDate());
            builder.paymentDeclarationExplanation(paymentDeclaration.getExplanation());
            builder.paymentDeclarationPaidAmount(paymentDeclaration.getPaidAmount().map(moneyMapper::to)
                .orElse(null));
        };
    }

    private FullDefenceResponse extractFullDefence(CCDCollectionElement<CCDRespondent> respondentElement) {
        CCDRespondent respondent = respondentElement.getValue();

        return FullDefenceResponse.builder()
            .defendant(defendantPartyMapper.from(respondentElement))
            .statementOfTruth(extractStatementOfTruth(respondent))
            .moreTimeNeeded(getMoreTimeNeeded(respondent))
            .freeMediation(getFreeMediation(respondent))
            .mediationPhoneNumber(telephoneMapper.from(
                respondent.getResponseMediationPhoneNumber()))
            .mediationContactPerson(respondent.getResponseMediationContactPerson())
            .defenceType(DefenceType.valueOf(respondent.getResponseDefenceType().name()))
            .defence(respondent.getResponseDefence())
            .evidence(extractDefendantEvidence(respondent))
            .timeline(extractDefendantTimeline(respondent))
            .paymentDeclaration(extractPaymentDeclaration(respondent))
            .directionsQuestionnaire(directionsQuestionnaireMapper.from(respondent.getDirectionsQuestionnaire()))
            .responseMethod(respondent.getResponseMethod() == null ? null :
                ResponseMethod.valueOf(respondent.getResponseMethod().name()))
            .build();
    }

    private StatementOfTruth extractStatementOfTruth(CCDRespondent respondent) {
        String signerName = respondent.getResponseDefendantSOTSignerName();
        String signerRole = respondent.getResponseDefendantSOTSignerRole();
        if (isAllBlank(signerName, signerRole)) {
            return null;
        } else {
            return new StatementOfTruth(signerName, signerRole);
        }
    }

    private PaymentDeclaration extractPaymentDeclaration(CCDRespondent respondent) {
        LocalDate paidDate = respondent.getPaymentDeclarationPaidDate();
        String explanation = respondent.getPaymentDeclarationExplanation();
        BigDecimal paidAmount = moneyMapper.from(respondent.getPaymentDeclarationPaidAmount());

        if (paidDate == null && paidAmount == null && explanation == null) {
            return null;
        }
        return new PaymentDeclaration(paidDate, paidAmount, explanation);
    }

    private DefendantTimeline extractDefendantTimeline(CCDRespondent respondent) {
        return new DefendantTimeline(
            asStream(respondent.getDefendantTimeLineEvents())
                .map(timelineEventMapper::from)
                .collect(Collectors.toList()),
            respondent.getDefendantTimeLineComment()
        );
    }

    private DefendantEvidence extractDefendantEvidence(CCDRespondent respondent) {
        return new DefendantEvidence(
            asStream(respondent.getResponseEvidenceRows())
                .map(evidenceRowMapper::from)
                .collect(Collectors.toList()),
            respondent.getResponseEvidenceComment()
        );
    }

    private PartAdmissionResponse extractPartAdmission(CCDCollectionElement<CCDRespondent> respondentElement) {
        CCDRespondent respondent = respondentElement.getValue();

        return PartAdmissionResponse.builder()
            .defendant(defendantPartyMapper.from(respondentElement))
            .amount(moneyMapper.from(respondent.getResponseAmount()))
            .defendant(defendantPartyMapper.from(respondentElement))
            .statementOfTruth(extractStatementOfTruth(respondent))
            .moreTimeNeeded(getMoreTimeNeeded(respondent))
            .freeMediation(getFreeMediation(respondent))
            .mediationPhoneNumber(telephoneMapper.from(
                respondent.getResponseMediationPhoneNumber()))
            .mediationContactPerson(respondent.getResponseMediationContactPerson())
            .paymentDeclaration(extractPaymentDeclaration(respondent))
            .paymentIntention(paymentIntentionMapper.from(respondent.getDefendantPaymentIntention()))
            .defence(respondent.getResponseDefence())
            .evidence(extractDefendantEvidence(respondent))
            .timeline(extractDefendantTimeline(respondent))
            .statementOfMeans(statementOfMeansMapper.from(respondent.getStatementOfMeans()))
            .directionsQuestionnaire(directionsQuestionnaireMapper.from(respondent.getDirectionsQuestionnaire()))
            .responseMethod(respondent.getResponseMethod() == null ? null :
                ResponseMethod.valueOf(respondent.getResponseMethod().name()))
            .build();
    }

    private YesNoOption getMoreTimeNeeded(CCDRespondent respondent) {
        return respondent.getResponseMoreTimeNeededOption() != null
            ? YesNoOption.valueOf(respondent.getResponseMoreTimeNeededOption().name())
            : null;
    }

    private FullAdmissionResponse extractFullAdmission(CCDCollectionElement<CCDRespondent> respondentElement) {
        CCDRespondent respondent = respondentElement.getValue();

        return FullAdmissionResponse.builder()
            .defendant(defendantPartyMapper.from(respondentElement))
            .statementOfTruth(extractStatementOfTruth(respondent))
            .moreTimeNeeded(getMoreTimeNeeded(respondent))
            .freeMediation(getFreeMediation(respondent))
            .mediationPhoneNumber(telephoneMapper.from(
                respondent.getResponseMediationPhoneNumber()))
            .mediationContactPerson(respondent.getResponseMediationContactPerson())
            .paymentIntention(paymentIntentionMapper.from(respondent.getDefendantPaymentIntention()))
            .statementOfMeans(statementOfMeansMapper.from(respondent.getStatementOfMeans()))
            .responseMethod(respondent.getResponseMethod() == null ? null :
                ResponseMethod.valueOf(respondent.getResponseMethod().name()))
            .build();
    }

    private YesNoOption getFreeMediation(CCDRespondent respondent) {
        return respondent.getResponseFreeMediationOption() != null
            ? YesNoOption.valueOf(respondent.getResponseFreeMediationOption().name())
            : null;
    }
}
