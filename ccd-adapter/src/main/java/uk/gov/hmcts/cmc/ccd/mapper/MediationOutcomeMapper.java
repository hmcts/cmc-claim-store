package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.MediationOutcome;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.FAILED;
import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.SUCCEEDED;

public class MediationOutcomeMapper {

    public static MediationOutcome from(CCDCase ccdCase){

        CCDRespondent defendant = ccdCase.getRespondents()
            .stream().findFirst().map(CCDCollectionElement::getValue).orElseThrow(IllegalStateException::new);

        if(Optional.ofNullable(defendant.getMediationFailedReason()).isPresent()){
            return FAILED;
        }else if (Optional.ofNullable(defendant.getMediationSettlementReachedAt()).isPresent()){
            return SUCCEEDED;
        }else {
            return null;
        }
    }
}
