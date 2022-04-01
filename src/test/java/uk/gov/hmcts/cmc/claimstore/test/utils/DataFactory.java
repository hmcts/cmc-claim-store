package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.cmc.ccd.domain.*;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;

import java.util.Set;

/**
 * A utility class that aids in the object creation of test data.
 */
@UtilityClass
public class DataFactory {

    public static Set<String> createStringSetFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static HearingCourt createHearingCourtFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static CCDCase createCCDCitizenCaseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

}
