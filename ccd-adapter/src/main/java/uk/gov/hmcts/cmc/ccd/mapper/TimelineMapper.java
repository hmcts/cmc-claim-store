package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Timeline;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TimelineMapper implements BuilderMapper<CCDCase, Timeline, CCDCase.CCDCaseBuilder> {

    private final TimelineEventMapper timelineEventMapper;

    @Autowired
    public TimelineMapper(TimelineEventMapper timelineEventMapper) {
        this.timelineEventMapper = timelineEventMapper;
    }

    @Override
    public void to(Timeline timeline, CCDCase.CCDCaseBuilder builder) {
        if (timeline == null) {
            return;
        }

        builder.timeline(
            timeline.getEvents()
                .stream()
                .map(timelineEventMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

    }

    @Override
    public Timeline from(CCDCase ccdCase) {
        if (ccdCase.getTimeline() == null || ccdCase.getTimeline().isEmpty()) {
            return null;
        }
        return new Timeline(
            ccdCase.getTimeline().stream()
                .map(timelineEventMapper::from)
                .collect(Collectors.toList())
        );
    }
}
