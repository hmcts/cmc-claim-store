package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class CaseDataExtractorUtils {

    private CaseDataExtractorUtils(){
        // NO-OP
    }

    public static List<String> getRecipient(BulkPrintRequestType bulkPrintRequestType, Claim claim){
        switch (bulkPrintRequestType) {
            case DIRECTION_ORDER_LETTER_TYPE, GENERAL_LETTER_TYPE -> getClaimant(claim);
        }
        return List.of();
    }

    public static List<String> getDefendant(Claim claim) {
        return List
            .of(requireNonNull(claim
                .getClaimData()
                .getDefendant()
                .getName()
            ));
    }

    public static List<String> getClaimant(Claim claim) {
        return List
            .of(requireNonNull(claim
                .getClaimData()
                .getClaimant()
                .getName()
            ));
    }

}
