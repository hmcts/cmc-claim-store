package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

/**
 * TODO : DESCRIPTION
 */
@UtilityClass
public class DataFactory {

    /**
     * Creates CourtFinderResponse from JSON File
     *
     * @param jsonFile for a provided json file
     * @return {@linkplain CourtFinderResponse}
     */
    public static CourtFinderResponse createCourtFinderResponseFromJson(String jsonFile) {
        String jsonContents = FileReaderUtils.readJsonFromFile(jsonFile);
        return JsonParserUtils.fromJson(jsonContents, new TypeReference<CourtFinderResponse>() {
        });
    }

}
