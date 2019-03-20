package uk.gov.hmcts.cmc.ccd.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapperUtil {

    private MapperUtil(){
        // Utility class, no instances
    }

    public static boolean isAllNull(Object... objects) {
        return Stream.of(objects).allMatch(Objects::nonNull);
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }

    public static Function<Claim, String> toCaseName = claim -> {
        String caseName = StringUtils.EMPTY;

        if(claim.getResponse().isPresent()){
            caseName = createCaseNameFromResponse(claim.getResponse().get());
        }else {
            caseName = claim.getClaimData().getClaimant().getName() + claim.getClaimData().getDe
        }

        return caseName;
    };

    private static String createCaseNameFromResponse(Response response){
        response.getDefendant().getName();

    }

    private static String createCaseNameFromClaimantProvidedDetail()
}
