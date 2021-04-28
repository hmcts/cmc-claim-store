package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;

import java.util.LinkedHashMap;

@Service
public class DirectionOrderService {

    private PilotCourtService pilotCourtService;

    public DirectionOrderService(PilotCourtService pilotCourtService) {
        this.pilotCourtService = pilotCourtService;
    }

    public HearingCourt getHearingCourt(CCDCase ccdCase) {
        if (ccdCase.getHearingCourt() instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> hm = (LinkedHashMap) ccdCase.getHearingCourt();
            LinkedHashMap<String, Object> hm1 = (LinkedHashMap) hm.get("value");
            if (StringUtils.isBlank(String.valueOf(hm1.get("code")))
                || (String.valueOf(hm1.get("code"))).equals(PilotCourtService.OTHER_COURT_ID)) {

                return HearingCourt.builder()
                    .name(ccdCase.getHearingCourtName())
                    .address(ccdCase.getHearingCourtAddress())
                    .build();
            }
            return pilotCourtService.getPilotHearingCourt(String.valueOf(hm1.get("code")))
                .orElseThrow(() -> new IllegalArgumentException("Court is not a pilot court: "
                    + ccdCase.getHearingCourt()));
        } else {
            if (StringUtils.isBlank((String) ccdCase.getHearingCourt())
                || ccdCase.getHearingCourt().equals(PilotCourtService.OTHER_COURT_ID)) {

                return HearingCourt.builder()
                    .name(ccdCase.getHearingCourtName())
                    .address(ccdCase.getHearingCourtAddress())
                    .build();
            }

            return pilotCourtService.getPilotHearingCourt((String) ccdCase.getHearingCourt())
                .orElseThrow(() -> new IllegalArgumentException("Court is not a pilot court: "
                    + ccdCase.getHearingCourt()));

        }

    }
}
