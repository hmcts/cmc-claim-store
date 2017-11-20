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
        final CCDClaim.CCDClaimBuilder builder = CCDClaim.builder();
        claimData.getFeeCode().ifPresent(builder::feeCode);
        claimData.getFeeAccountNumber().ifPresent(builder::feeAccountNumber);
        claimData.getExternalReferenceNumber().ifPresent(builder::externalReferenceNumber);
        claimData.getPreferredCourt().ifPresent(builder::preferredCourt);

        claimData.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        claimData.getPersonalInjury()
            .ifPresent(personalInjury -> builder.personalInjury(personalInjuryMapper.to(personalInjury)));

        claimData.getHousingDisrepair().ifPresent(housingDisrepair -> housingDisrepairMapper.to(housingDisrepair));

        // TODO: amount field mapping is pending

        return builder
            .reason(claimData.getReason())
            .feeAmountInPennies(claimData.getFeeAmountInPennies())
            .externalId(claimData.getExternalId().toString())
            .build();
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        return null;
    }
}
