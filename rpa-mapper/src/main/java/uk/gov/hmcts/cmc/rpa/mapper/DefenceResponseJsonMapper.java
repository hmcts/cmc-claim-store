package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.math.BigDecimal;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class DefenceResponseJsonMapper {

    @Autowired
    private final DefendantJsonMapper defendantMapper;

    public DefenceResponseJsonMapper(DefendantJsonMapper defendantMapper) {
        this.defendantMapper = defendantMapper;
    }

    public JsonObject map(Claim claim) {
        Response response = claim.getResponse()
            .orElseThrow(() -> new IllegalArgumentException("Missing response"));
        String defendantsEmail = claim.getDefendantEmail();

        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("responseSubmittedOn", DateFormatter.format(claim.getRespondedAt()))
            .add("defenceResponse", defenceResponse(response))
            .add("defendant", defendantMapper.map(response.getDefendant(), claim.getClaimData().getDefendant(), defendantsEmail))
            .add("mediation", isMediationSelected(response))
            .add("amountAdmitted", getAmountAdmitted(response))
            .add("payment", mapPayment(response))
            .add("hearingRequirements", areHearingRequirementsRequested(claim, response))
            .build();
    }

    private BigDecimal getAmountAdmitted(Response response) {
        if (response instanceof PartAdmissionResponse) {
            return ((PartAdmissionResponse) response).getAmount();
        }
        return null;
    }

    private JsonObject mapPayment(Response response) {
        if (ResponseUtils.isAdmissionResponse(response)) {
            JsonObjectBuilder builder = updateJsonBuilderForAdmissions(response);
            if (builder != null) {
                return builder.build();
            }
        }
        return null;
    }

    private JsonObjectBuilder updateJsonBuilderForAdmissions(Response response) {
        PaymentIntention paymentIntention = null;
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder();
        if (response instanceof PartAdmissionResponse) {
            PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
            if (partAdmissionResponse.getPaymentDeclaration().isPresent()) {
                return null;
            }
            paymentIntention = partAdmissionResponse.getPaymentIntention()
                .orElseThrow(() -> new IllegalArgumentException("Missing payment intention"));
        } else if (response instanceof FullAdmissionResponse) {
            FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
            paymentIntention = fullAdmissionResponse.getPaymentIntention();
        }
        requireNonNull(paymentIntention);
        PaymentOption paymentOption = paymentIntention.getPaymentOption();
        jsonObjectBuilder.add("paymentType", paymentOption.name());
        String fullPaymentDeadLine = paymentOption == PaymentOption.BY_SPECIFIED_DATE
            ? paymentIntention.getPaymentDate().map(DateFormatter::format)
                .orElseThrow(() -> new IllegalArgumentException("Missing payment date"))
            : null;
        jsonObjectBuilder.add("fullPaymentDeadline", fullPaymentDeadLine);
        JsonObject installmentObj = paymentOption == PaymentOption.INSTALMENTS
            ? RPAMapperHelper.toJson(paymentIntention.getRepaymentPlan()
                .orElseThrow(() -> new IllegalArgumentException("Missing repayment plan")))
            : null;
        jsonObjectBuilder.add("instalments", installmentObj);

        return jsonObjectBuilder;

    }

    private String defenceResponse(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                return extractFromSubclass(response, FullDefenceResponse.class,
                    fullDefenceResponse -> fullDefenceResponse.getDefenceType().name());
            case FULL_ADMISSION:
                return response.getResponseType().name();
            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                return partAdmissionResponse.getPaymentDeclaration().map(PaymentDeclaration::getPaidDate).isPresent()
                    ? DefenceType.ALREADY_PAID.name()
                    : response.getResponseType().name();
            default:
                throw new IllegalArgumentException("Invalid response type: " + response.getResponseType());
        }
    }

    private boolean isMediationSelected(Response response) {
        return response.getFreeMediation().orElse(YesNoOption.NO) == YesNoOption.YES;
    }

    private boolean areHearingRequirementsRequested(Claim claim, Response response) {
        if (claim.getFeatures() == null || !claim.getFeatures().contains("directionsQuestionnaire")) {
            return false;
        }

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                FullDefenceResponse fullDefenceResponse = (FullDefenceResponse)response;
                return fullDefenceResponse.getDirectionsQuestionnaire().isPresent();
            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse)response;
                return partAdmissionResponse.getDirectionsQuestionnaire().isPresent();
            case FULL_ADMISSION:
            default:
                return false;
        }
    }

}
