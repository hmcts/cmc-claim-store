package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDCourtLocationOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DirectionsQuestionnaireMapper implements Mapper<CCDDirectionsQuestionnaire, DirectionsQuestionnaire> {

    private final ExpertRowMapper expertRowMapper;
    private final UnavailableDateMapper unavailableDateMapper;
    private final AddressMapper addressMapper;

    @Autowired
    public DirectionsQuestionnaireMapper(
        ExpertRowMapper expertRowMapper,
        UnavailableDateMapper unavailableDateMapper,
        AddressMapper addressMapper
    ) {
        this.expertRowMapper = expertRowMapper;
        this.unavailableDateMapper = unavailableDateMapper;
        this.addressMapper = addressMapper;
    }

    @Override
    public CCDDirectionsQuestionnaire to(DirectionsQuestionnaire directionsQuestionnaire) {
        if (directionsQuestionnaire == null) {
            return null;
        }

        final CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder = CCDDirectionsQuestionnaire.builder();
        directionsQuestionnaire.getRequireSupport().ifPresent(requireSupport -> {
            requireSupport.getLanguageInterpreter().ifPresent(builder::languageInterpreted);
            requireSupport.getSignLanguageInterpreter().ifPresent(builder::signLanguageInterpreted);
            requireSupport.getOtherSupport().ifPresent(builder::otherSupportRequired);

            requireSupport.getHearingLoop()
                .map(YesNoOption::name)
                .map(CCDYesNoOption::valueOf)
                .ifPresent(builder::hearingLoop);

            requireSupport.getDisabledAccess()
                .map(YesNoOption::name)
                .map(CCDYesNoOption::valueOf)
                .ifPresent(builder::disabledAccess);
        });

        toHearingLocation(directionsQuestionnaire.getHearingLocation(), builder);

        directionsQuestionnaire.getWitness().ifPresent(witness -> {
            builder.selfWitness(CCDYesNoOption.valueOf(witness.getSelfWitness().name()));
            witness.getNoOfOtherWitness().ifPresent(builder::howManyOtherWitness);
        });

        directionsQuestionnaire.getExpertRequest().ifPresent(expertRequest -> {
            builder.expertEvidenceToExamine(expertRequest.getExpertEvidenceToExamine());
            builder.reasonForExpertAdvice(expertRequest.getReasonForExpertAdvice());
        });

        builder.expertReports(directionsQuestionnaire.getExpertReports()
            .stream()
            .map(expertRowMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        builder.unavailableDates(directionsQuestionnaire.getUnavailableDates()
            .stream()
            .map(unavailableDateMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        return builder.build();
    }

    private void toHearingLocation(
        HearingLocation hearingLocation,
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        builder.hearingLocation(hearingLocation.getCourtName());
        builder.hearingLocationSlug(hearingLocation.getHearingLocationSlug());
        hearingLocation.getCourtAddress()
            .ifPresent(address -> builder.hearingCourtAddress(addressMapper.to(address)));
        builder.hearingLocationOption(CCDCourtLocationOption.valueOf(hearingLocation.getLocationOption().name()));
        hearingLocation.getExceptionalCircumstancesReason().ifPresent(builder::exceptionalCircumstancesReason);
    }

    @Override
    public DirectionsQuestionnaire from(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        if (ccdDirectionsQuestionnaire == null) {
            return null;
        }

        return DirectionsQuestionnaire.builder()
            .build();
    }


}
