package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;

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
    private YesNoOption defendantPinLetterUpload;

    public static ClaimSubmissionOperationIndicatorsBuilder builder() {
        return new ClaimSubmissionOperationIndicatorsBuilder() {
            @Override
            public ClaimSubmissionOperationIndicators build() {
                super.claimantNotification = setDefaultIfNull(super.claimantNotification);
                super.defendantNotification = setDefaultIfNull(super.defendantNotification);
                super.bulkPrint = setDefaultIfNull(super.bulkPrint);
                super.rpa = setDefaultIfNull(super.rpa);
                super.staffNotification = setDefaultIfNull(super.staffNotification);
                super.sealedClaimUpload = setDefaultIfNull(super.sealedClaimUpload);
                super.claimIssueReceiptUpload = setDefaultIfNull(super.claimIssueReceiptUpload);
                super.defendantPinLetterUpload = setDefaultIfNull(super.defendantPinLetterUpload);

                return super.build();
            }

            private YesNoOption setDefaultIfNull(YesNoOption prop) {
                return Objects.isNull(prop) ? YesNoOption.NO : prop;
            }
        };
    }
}
