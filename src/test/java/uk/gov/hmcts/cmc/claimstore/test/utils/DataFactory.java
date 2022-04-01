package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.cmc.ccd.domain.*;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;

import java.util.List;
import java.util.Set;

/**
 * A utility class that aids in the object creation of test data.
 */
@UtilityClass
public class DataFactory {

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

    public static CCDCase createCCDCitizenCaseFromJson(String jsonFileSrc) {
        return JsonParserUtils.fromJson(jsonFileSrc, new TypeReference<>() {
        });
    }

    public static CCDCase getCCDData(String postcode, CCDCase.CCDCaseBuilder builder, CCDPartyType company) {
        return builder
            .features(ClaimFeatures.DQ_FLAG.getValue())
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(CCDRespondent.builder()
                    .partyDetail(CCDParty.builder()
                        .primaryAddress(CCDAddress.builder().postCode(postcode).build())
                        .build())
                    .claimantProvidedDetail(CCDParty.builder()
                        .emailAddress("abc@def.com")
                        .type(company)
                        .build())
                    .build())
                .build()))
            .applicants(
                com.google.common.collect.ImmutableList.of(
                    CCDCollectionElement.<CCDApplicant>builder()
                        .value(SampleData.getCCDApplicantIndividual())
                        .build()
                ))
            .build();
    }

}
