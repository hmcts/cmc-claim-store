package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.util.stream.Collectors;

@Component
public class ClaimMapper implements Mapper<CCDClaim, ClaimData> {

    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;
    private final TheirDetailsMapper theirDetailsMapper;
    private final AmountMapper amountMapper;

    @Autowired
    public ClaimMapper(final PersonalInjuryMapper personalInjuryMapper,
                       final HousingDisrepairMapper housingDisrepairMapper,
                       final StatementOfTruthMapper statementOfTruthMapper,
                       final PartyMapper partyMapper,
                       final TheirDetailsMapper theirDetailsMapper,
                       final AmountMapper amountMapper) {

        this.personalInjuryMapper = personalInjuryMapper;
        this.housingDisrepairMapper = housingDisrepairMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
        this.partyMapper = partyMapper;
        this.theirDetailsMapper = theirDetailsMapper;
        this.amountMapper = amountMapper;
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

        claimData.getHousingDisrepair()
            .ifPresent(housingDisrepair -> builder.housingDisrepair(housingDisrepairMapper.to(housingDisrepair)));

        builder.claimants(claimData.getClaimants().stream().map(partyMapper::to).collect(Collectors.toList()));
        builder.defendants(claimData.getDefendants().stream().map(theirDetailsMapper::to).collect(Collectors.toList()));

        return builder
            .reason(claimData.getReason())
            .amount(amountMapper.to(claimData.getAmount()))
            .feeAmountInPennies(claimData.getFeeAmountInPennies())
            .externalId(claimData.getExternalId().toString())
            .build();
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        throw new NotImplementedException("Not implemented yet!");
    }
}
