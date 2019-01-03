package uk.gov.hmcts.cmc.ccd;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

public class SampleData {
    //Utility class
    private SampleData() {
    }



}
