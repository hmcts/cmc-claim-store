package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

@Component
public class ClaimMapper implements Mapper<CCDClaim, ClaimData> {

    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;

    @Autowired
    public ClaimMapper(PersonalInjuryMapper personalInjuryMapper, HousingDisrepairMapper housingDisrepairMapper,
                       StatementOfTruthMapper statementOfTruthMapper) {
        this.personalInjuryMapper = personalInjuryMapper;
        this.housingDisrepairMapper = housingDisrepairMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
    }

    @Override
    public CCDClaim to(ClaimData claimData) {
        return CCDClaim.builder()
            .reason(claimData.getReason())
            .feeCode(claimData.getFeeCode().orElse(""))
            .feeAccountNumber(claimData.getFeeAccountNumber().orElse(""))
            .feeAmountInPennies(claimData.getFeeAmountInPennies())
            .externalId(claimData.getExternalId().toString())
            .externalReferenceNumber(claimData.getExternalReferenceNumber().orElse(""))
            .build();
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        return null;
    }
}
