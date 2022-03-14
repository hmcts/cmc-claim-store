package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

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


}
