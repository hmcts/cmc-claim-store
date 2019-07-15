package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@Value
@Builder(toBuilder = true)
public class ClaimSubmissionOperationIndicators {

    private YesNoOption claimantNotification;
    private YesNoOption defendantNotification;
    private YesNoOption bulkPrint;
    private YesNoOption rpa;
    private YesNoOption staffNotification;
    private YesNoOption sealedClaimUpload;
    private YesNoOption claimIssueReceiptUpload;

    public static ClaimSubmissionOperationIndicatorsBuilder builder() {
        return new ClaimSubmissionOperationIndicatorsBuilder() {
            @Override
            public ClaimSubmissionOperationIndicators build() {
                super.claimantNotification = setDefaultIfNull(super.claimantNotification);
                super.bulkPrint = setDefaultIfNull(super.bulkPrint);
                super.staffNotification = setDefaultIfNull(super.staffNotification);
                super.sealedClaimUpload = setDefaultIfNull(super.sealedClaimUpload);

                return super.build();
            }

            private YesNoOption setDefaultIfNull(YesNoOption prop) {
                return Objects.isNull(prop) ? YesNoOption.NO : prop;
            }
        };
    }

    @JsonIgnore
    public boolean isPinOperationSuccess() {
        return Stream.of(
            bulkPrint,
            staffNotification,
            defendantNotification
        ).allMatch(ind -> ind.equals(YES));
    }

    @JsonIgnore
    public boolean isAllSuccess() {
        return Stream.of(
            claimantNotification,
            defendantNotification,
            bulkPrint,
            rpa,
            staffNotification,
            sealedClaimUpload,
            claimIssueReceiptUpload
        ).allMatch(option -> option == YES);
    }
}
