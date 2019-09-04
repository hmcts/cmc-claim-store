package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;

@Component
public class HearingCourtDetailsFinder {
    private final CourtFinderApi courtFinderApi;
    private final HearingCourtMapper hearingCourtMapper;

    public HearingCourtDetailsFinder(CourtFinderApi courtFinderApi, HearingCourtMapper hearingCourtMapper) {
        this.courtFinderApi = courtFinderApi;
        this.hearingCourtMapper = hearingCourtMapper;
    }

    public HearingCourt findHearingCourtAddress(CCDHearingCourtType courtType) {
        return courtFinderApi.findMoneyClaimCourtByPostcode(courtType.getPostcode())
            .stream()
            .findFirst()
            .map(hearingCourtMapper::from)
            .orElseThrow(IllegalArgumentException::new);
    }
}
