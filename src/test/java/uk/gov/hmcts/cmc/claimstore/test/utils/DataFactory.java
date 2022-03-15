package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;

import java.util.Set;

/**
 * A utility class that aids in the object creation of test data
 */
@UtilityClass
public class DataFactory {

    /**
     * Creates CourtFinderResponse from JSON File
     *
     * @param jsonFileSrc for a provided json file
     * @return {@linkplain CourtFinderResponse}
     */
    public static CourtFinderResponse createCourtFinderResponseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static Set<String> createStringSetFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static CourtDetails createCourtDetailsFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static HearingCourt createHearingCourtFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }


}
