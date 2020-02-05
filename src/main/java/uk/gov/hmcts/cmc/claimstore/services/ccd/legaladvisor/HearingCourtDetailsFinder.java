package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType.OTHER;

@Component
public class HearingCourtDetailsFinder {
    private final CourtFinderApi courtFinderApi;
    private final HearingCourtMapper hearingCourtMapper;

    public HearingCourtDetailsFinder(CourtFinderApi courtFinderApi, HearingCourtMapper hearingCourtMapper) {
        this.courtFinderApi = courtFinderApi;
        this.hearingCourtMapper = hearingCourtMapper;
    }

    public HearingCourt findHearingCourtAddress(CCDCase ccdCase) {
        return Optional.ofNullable(ccdCase.getHearingCourt())
            .filter(c -> c != OTHER)
            .map(this::mapHearingCourt)
            .orElse(
                HearingCourt.builder()
                .name(ccdCase.getHearingCourtName())
                .address(ccdCase.getHearingCourtAddress())
                .build());
    }

    private HearingCourt mapHearingCourt(CCDHearingCourtType courtType) {
        return courtFinderApi.findMoneyClaimCourtByPostcode(courtType.getPostcode())
            .stream()
            .findFirst()
            .map(hearingCourtMapper::from)
            .orElseThrow(IllegalArgumentException::new);
    }
}
