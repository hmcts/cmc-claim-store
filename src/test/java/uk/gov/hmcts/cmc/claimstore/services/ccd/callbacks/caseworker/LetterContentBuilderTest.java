package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LetterContentBuilderTest {
    private LetterContentBuilder letterContentBuilder = new LetterContentBuilder();

    @Test
    void shouldReturnLetterContentWithoutChanges() {
        CCDParty partyA = SampleData.getCCDPartyWithEmail("j@bfdj.com");
        CCDParty partyB = SampleData.getCCDPartyWithEmail("j@bfdj.com");
        CCDContactChangeContent ccdContactChangeContent = letterContentBuilder.letterContent(partyA, partyB);
        assertTrue(ccdContactChangeContent.noContentChange());
        assertAllRemovedFlagsAreFalse(ccdContactChangeContent);
    }

    @Test
    void shouldReturnLetterContentWithChanges() {
        CCDParty partyA = SampleData.getCCDPartyWithEmail("j@bfdj.com");
        String modifiedEmail = "changed@bfdj.com";
        CCDAddress modifiedAddress = SampleData.getCCDAddress().toBuilder().addressLine1("changedLine1").build();
        CCDTelephone modifiedTelephone = CCDTelephone.builder().telephoneNumber("01234567891").build();

        CCDParty partyWithUpdatedDetails = partyA.toBuilder()
            .telephoneNumber(modifiedTelephone)
            .primaryAddress(modifiedAddress)
            .correspondenceAddress(modifiedAddress)
            .emailAddress(modifiedEmail)
            .build();

        CCDContactChangeContent ccdContactChangeContent
            = letterContentBuilder.letterContent(partyA, partyWithUpdatedDetails);

        assertFalse(ccdContactChangeContent.noContentChange());
        assertThat(ccdContactChangeContent.getIsEmailModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsTelephoneModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsPrimaryAddressModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsCorrespondenceAddressModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getPrimaryEmail()).isEqualTo(modifiedEmail);
        assertThat(ccdContactChangeContent.getTelephone()).isEqualTo(modifiedTelephone.getTelephoneNumber());
        assertThat(ccdContactChangeContent.getPrimaryAddress()).isEqualTo(modifiedAddress);
        assertThat(ccdContactChangeContent.getCorrespondenceAddress()).isEqualTo(modifiedAddress);

        assertAllRemovedFlagsAreFalse(ccdContactChangeContent);
    }

    @Test
    void shouldReturnLetterContentWithContentRemovedFlags() {
        CCDParty party = SampleData.getCCDPartyWithEmail("j@bfdj.com");
        CCDParty partyWithUpdatedDetails = party.toBuilder()
            .telephoneNumber(null)
            .correspondenceAddress(null)
            .emailAddress(null)
            .build();

        CCDContactChangeContent ccdContactChangeContent
            = letterContentBuilder.letterContent(party, partyWithUpdatedDetails);

        assertFalse(ccdContactChangeContent.noContentChange());
        assertThat(ccdContactChangeContent.getIsEmailModified()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getIsTelephoneModified()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getIsPrimaryAddressModified()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getIsCorrespondenceAddressModified()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getPrimaryEmail()).isNull();
        assertThat(ccdContactChangeContent.getTelephone()).isNull();
        assertThat(ccdContactChangeContent.getCorrespondenceAddress()).isNull();

        assertThat(ccdContactChangeContent.getPrimaryEmailRemoved()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getTelephoneRemoved()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getCorrespondenceAddressRemoved()).isEqualTo(CCDYesNoOption.YES);
    }

    @Test
    void shouldReturnLetterContentWithNewContentAdded() {
        CCDParty partyWithNewDetails = SampleData.getCCDPartyWithEmail("j@bfdj.com");

        CCDContactChangeContent ccdContactChangeContent
            = letterContentBuilder.letterContent(null, partyWithNewDetails);

        assertFalse(ccdContactChangeContent.noContentChange());
        assertThat(ccdContactChangeContent.getIsEmailModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsTelephoneModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsPrimaryAddressModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getIsCorrespondenceAddressModified()).isEqualTo(CCDYesNoOption.YES);
        assertThat(ccdContactChangeContent.getPrimaryEmail()).isEqualTo(partyWithNewDetails.getEmailAddress());
        assertThat(ccdContactChangeContent.getTelephone())
            .isEqualTo(partyWithNewDetails.getTelephoneNumber().getTelephoneNumber());
        assertThat(ccdContactChangeContent.getCorrespondenceAddress())
            .isEqualTo(partyWithNewDetails.getCorrespondenceAddress());

        assertAllRemovedFlagsAreFalse(ccdContactChangeContent);
    }

    private void assertAllRemovedFlagsAreFalse(CCDContactChangeContent ccdContactChangeContent) {
        assertThat(ccdContactChangeContent.getPrimaryEmailRemoved()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getTelephoneRemoved()).isEqualTo(CCDYesNoOption.NO);
        assertThat(ccdContactChangeContent.getCorrespondenceAddressRemoved()).isEqualTo(CCDYesNoOption.NO);
    }
}
