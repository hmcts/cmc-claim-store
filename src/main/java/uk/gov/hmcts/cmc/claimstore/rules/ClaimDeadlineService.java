package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ClaimDeadlineService {

    public boolean isPastDeadline(LocalDateTime currentTime, LocalDate deadlineDay) {
        LocalDateTime responseDeadlineTime = deadlineDay.atTime(16, 0);
        return currentTime.isEqual(responseDeadlineTime) || currentTime.isAfter(responseDeadlineTime);
    }

}
