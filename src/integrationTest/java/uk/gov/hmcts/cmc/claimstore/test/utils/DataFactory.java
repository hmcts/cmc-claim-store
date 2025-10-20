package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name.SearchCourtByNameResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.SearchCourtByPostcodeResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug.SearchCourtBySlugResponse;
import java.util.List;

/**
 * A utility class that aids in the object creation of test data.
 */
public class DataFactory {

    private DataFactory() {
        throw new RuntimeException();
    }

    public static Court createCourtFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static List<Court> createCourtListFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static SearchCourtByNameResponse createSearchCourtByNameResponseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static SearchCourtByPostcodeResponse createSearchCourtByPostcodeResponseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static SearchCourtBySlugResponse createSearchCourtBySlugResponseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

}
