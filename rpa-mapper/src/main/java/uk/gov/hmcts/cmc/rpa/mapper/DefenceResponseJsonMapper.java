package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
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
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        String defendantsEmail = claim.getDefendantEmail();

        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("responseSubmittedOn", DateFormatter.format(claim.getRespondedAt()))
            .add("defenceResponse", defenceResponse(response))
            .add("defendant", defendantMapper.map(response.getDefendant(), claim.getClaimData().getDefendant(), defendantsEmail))
            .add("mediation", isMediationSelected(response))
            .add("amountAdmitted", getAmountAdmitted(response))
            .add("payment", mapPayment(response))
            .build();
    }

    private BigDecimal getAmountAdmitted(Response response) {
        if (response instanceof PartAdmissionResponse) {
            return ((PartAdmissionResponse) response).getAmount();
        }
        return null;
    }

    private JsonObject mapPayment(Response response) {
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder();
        if (ResponseUtils.isAdmissionResponse(response)) {
            jsonObjectBuilder = retrievePaymentBuilder(jsonObjectBuilder, response);
            return jsonObjectBuilder.build();
        } else {
            return null;
        }
    }

    private JsonObjectBuilder retrievePaymentBuilder(JsonObjectBuilder jsonObjectBuilder, Response response) {
        PaymentIntention paymentIntention = null;
        if (response instanceof PartAdmissionResponse) {
            PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
            paymentIntention = partAdmissionResponse.getPaymentIntention().orElseThrow(IllegalArgumentException::new);
        } else if (response instanceof FullAdmissionResponse) {
            FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
            paymentIntention = fullAdmissionResponse.getPaymentIntention();
        }
        PaymentOption paymentOption = paymentIntention.getPaymentOption();
        jsonObjectBuilder.add("paymentType", paymentOption.name());
        String fullPaymentDeadLine = paymentOption.equals(PaymentOption.BY_SPECIFIED_DATE)
            ? paymentIntention.getPaymentDate().map(DateFormatter::format)
                .orElseThrow(IllegalArgumentException::new) : null;
        jsonObjectBuilder.add("fullPaymentDeadline", fullPaymentDeadLine);
        jsonObjectBuilder.add("instalments", RPAMapperHelper
            .toJson(paymentIntention.getRepaymentPlan().orElse(null)));

        return jsonObjectBuilder;
    }

    private String defenceResponse(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                return extractFromSubclass(response, FullDefenceResponse.class,
                    fullDefenceResponse -> fullDefenceResponse.getDefenceType().name());
            case FULL_ADMISSION:
            case PART_ADMISSION:
                return response.getResponseType().name();
            default:
                throw new IllegalArgumentException("Invalid response type: " + response.getResponseType());
        }
    }

    private boolean isMediationSelected(Response response) {
        if (FullDefenceResponse.class.isInstance(response)
            && ((FullDefenceResponse) response).getDefenceType().equals(DefenceType.DISPUTE)) {
            YesNoOption yesNoOption = response.getFreeMediation().orElse(YesNoOption.NO);
            if (yesNoOption.equals(YesNoOption.YES)) {
                return true;
            }
        }
        return false;
    }
}
