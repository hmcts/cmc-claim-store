package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;

@Service
public class DirectionOrderService {

    private PilotCourtService pilotCourtService;

    public DirectionOrderService(PilotCourtService pilotCourtService) {
        this.pilotCourtService = pilotCourtService;
    }

    public HearingCourt getHearingCourt(CCDCase ccdCase) {
        if (StringUtils.isBlank(ccdCase.getHearingCourt())
            || ccdCase.getHearingCourt().equals(PilotCourtService.OTHER_COURT_ID)) {

            return  HearingCourt.builder()
                .name(ccdCase.getHearingCourtName())
                .address(ccdCase.getHearingCourtAddress())
                .build();
        }

        return pilotCourtService.getPilotHearingCourt(ccdCase.getHearingCourt())
            .orElseThrow(() -> new IllegalArgumentException("Court is not a pilot court: "
                + ccdCase.getHearingCourt()));

    }
}
