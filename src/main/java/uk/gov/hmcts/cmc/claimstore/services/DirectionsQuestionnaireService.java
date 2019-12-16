package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DirectionsQuestionnaireService {
    private final CaseRepository caseRepository;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;

    public DirectionsQuestionnaireService(
            CaseRepository caseRepository,
            DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator
    ) {
        this.caseRepository = caseRepository;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
    }

    public LocalDate updateDirectionsQuestionnaireDeadline(Claim claim, LocalDateTime from, String authorization) {
        LocalDate deadline = directionsQuestionnaireDeadlineCalculator.calculateDirectionsQuestionnaireDeadline(from);
        caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
        return deadline;
    }
}
