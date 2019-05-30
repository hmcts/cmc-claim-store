package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class StatesPaidMetadata {
    private final LocalDate paidDate;
    private final Boolean fullAmount;

    static StatesPaidMetadata fromClaim(Claim claim) {
        final Optional<Response> optionalResponse = claim.getResponse();
        if (!optionalResponse.isPresent()) {
            return null;
        }
        final Response response = optionalResponse.get();

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                final FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) response;
                return extractStatesPaidMetadata(claim, fullDefenceResponse::getPaymentDeclaration);

            case PART_ADMISSION:
                final PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                return extractStatesPaidMetadata(claim, partAdmissionResponse::getPaymentDeclaration);

            default:
                return null;
        }
    }

    private static StatesPaidMetadata extractStatesPaidMetadata(
        Claim claim,
        Supplier<Optional<PaymentDeclaration>> paymentDeclarationSupplier
    ) {
        final Optional<PaymentDeclaration> optionalPaymentDeclaration = paymentDeclarationSupplier.get();
        if (!optionalPaymentDeclaration.isPresent()) {
            return null;
        }
        final PaymentDeclaration declaration = optionalPaymentDeclaration.get();

        return new StatesPaidMetadata(
            declaration.getPaidDate(),
            declaration.getPaidAmount()
                .filter(Predicate.isEqual(claim.getTotalAmountTillToday().orElse(null)))
                .isPresent()
        );
    }
}
