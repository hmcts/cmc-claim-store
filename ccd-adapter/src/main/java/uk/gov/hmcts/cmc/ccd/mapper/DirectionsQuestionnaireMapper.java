package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDCourtLocationOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

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

        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder = CCDDirectionsQuestionnaire.builder();
        directionsQuestionnaire.getRequireSupport().ifPresent(toRequireSupport(builder));

        toHearingLocation(directionsQuestionnaire.getHearingLocation(), builder);

        directionsQuestionnaire.getWitness().ifPresent(toWitness(builder));

        directionsQuestionnaire.getExpertRequest().ifPresent(toExpertRequest(builder));

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

    private Consumer<RequireSupport> toRequireSupport(
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        return requireSupport -> {
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
        };
    }

    private Consumer<ExpertRequest> toExpertRequest(
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        return expertRequest -> {
            builder.expertEvidenceToExamine(expertRequest.getExpertEvidenceToExamine());
            builder.reasonForExpertAdvice(expertRequest.getReasonForExpertAdvice());
        };
    }

    private Consumer<Witness> toWitness(CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder) {
        return witness -> {
            builder.selfWitness(CCDYesNoOption.valueOf(witness.getSelfWitness().name()));
            witness.getNoOfOtherWitness().ifPresent(builder::howManyOtherWitness);
        };
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

        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder = DirectionsQuestionnaire.builder();
        fromHearingLocation(builder, ccdDirectionsQuestionnaire);
        fromWitness(builder, ccdDirectionsQuestionnaire);
        fromExpertRequest(builder, ccdDirectionsQuestionnaire);
        fromRequireSupport(builder, ccdDirectionsQuestionnaire);

        List<ExpertReport> expertReports = asStream(ccdDirectionsQuestionnaire.getExpertReports())
            .map(expertRowMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder.expertReports(expertReports);

        List<UnavailableDate> unavailableDates = asStream(ccdDirectionsQuestionnaire.getUnavailableDates())
            .map(unavailableDateMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder.unavailableDates(unavailableDates);

        return builder.build();
    }

    private void fromRequireSupport(
        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder,
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {

    }

    private void fromExpertRequest(
        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder,
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {

    }

    private void fromWitness(
        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder,
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {

    }

    private void fromHearingLocation(
        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder,
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {

    }
}
