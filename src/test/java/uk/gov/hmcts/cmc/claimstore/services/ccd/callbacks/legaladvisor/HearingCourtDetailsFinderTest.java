package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtDetailsFinder;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
public class HearingCourtDetailsFinderTest {

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private HearingCourtMapper hearingCourtMapper;

    private HearingCourtDetailsFinder hearingCourtDetailsFinder;

    @BeforeEach
    void setUp() {
        hearingCourtDetailsFinder = new HearingCourtDetailsFinder(
            courtFinderApi,
            hearingCourtMapper
        );
    }

    @Nested
    @DisplayName("Pilot Court tests")
    class PilotCourtTests {

        private final CCDHearingCourtType courtType = CCDHearingCourtType.values()[0];

        @Test
        void shouldReturnValidHearingCourt() {
            when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                .thenReturn(Collections.singletonList(Court.builder().build()));
            when(hearingCourtMapper.from(any(Court.class)))
                .thenReturn(HearingCourt.builder().build());

            CCDCase ccdCase = CCDCase.builder().hearingCourt(courtType).build();
            hearingCourtDetailsFinder.getHearingCourt(ccdCase);

            verify(hearingCourtMapper, once()).from(any(Court.class));

        }

        @Test
        void shouldCallCourtFinderApi() {
            when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                .thenReturn(Collections.singletonList(Court.builder().build()));
            when(hearingCourtMapper.from(any(Court.class)))
                .thenReturn(HearingCourt.builder().build());

            CCDCase ccdCase = CCDCase.builder().hearingCourt(courtType).build();
            hearingCourtDetailsFinder.getHearingCourt(ccdCase);

            verify(courtFinderApi, once()).findMoneyClaimCourtByPostcode(eq(courtType.getPostcode()));

        }
    }

    @Nested
    @DisplayName("Other Court tests")
    class OtherCourtTests {

        private final CCDHearingCourtType courtType = CCDHearingCourtType.OTHER;

        private final String courtName = "Clerkenwell Court";
        private final CCDAddress courtAddress = CCDAddress.builder()
            .addressLine1("line1")
            .addressLine2("line2")
            .addressLine3("line3")
            .postCode("SW1P4BB")
            .postTown("Clerkenwell")
            .build();

        @Test
        void shouldReturnValidHearingCourt() {
            CCDCase ccdCase = CCDCase.builder()
                .hearingCourt(CCDHearingCourtType.OTHER)
                .hearingCourtName(courtName)
                .hearingCourtAddress(courtAddress)
                .build();
            HearingCourt hearingCourt = hearingCourtDetailsFinder.getHearingCourt(ccdCase);

            assertEquals(courtName, hearingCourt.getName());

            assertEquals(courtAddress.getAddressLine1(), hearingCourt.getAddress().getAddressLine1());
            assertEquals(courtAddress.getAddressLine2(), hearingCourt.getAddress().getAddressLine2());
            assertEquals(courtAddress.getAddressLine3(), hearingCourt.getAddress().getAddressLine3());
            assertEquals(courtAddress.getPostTown(), hearingCourt.getAddress().getPostTown());
            assertEquals(courtAddress.getPostCode(), hearingCourt.getAddress().getPostCode());

        }

        @Test
        void shouldNotCallCourtFinderApi() {

            CCDCase ccdCase = CCDCase.builder().hearingCourt(courtType).build();
            hearingCourtDetailsFinder.getHearingCourt(ccdCase);

            verify(hearingCourtMapper, never()).from(any(Court.class));

        }

    }

}
