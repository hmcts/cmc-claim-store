package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;

import java.time.LocalDate;

public class SampleCCDDefendant {

    public static CCDDefendant.CCDDefendantBuilder withDefault() {
        return CCDDefendant.builder()
            .claimantProvidedType(CCDPartyType.INDIVIDUAL)
            .defendantId("defendantId")
            .letterHolderId("JCJEDU")
            .responseDeadline(LocalDate.now().plusDays(14))
            .partyEmail("defendant@Ididabadjob.com");
    }

    public static CCDDefendant withResponseMoreTimeNeededOption() {
        return withDefault().responseMoreTimeNeededOption(CCDYesNoOption.NO)
            .build();
    }

    public static CCDDefendant withResponse() {
        return withDefault().responseMoreTimeNeededOption(CCDYesNoOption.NO)
            .responseDefence("Defenceeeee")
            .responseDefenceType(CCDDefenceType.ALREADY_PAID)
            .partyType(CCDPartyType.INDIVIDUAL)
            .partyName("PartEEEE")
            .responseFreeMediationOption(CCDYesNoOption.YES)
            .build();
    }
}
