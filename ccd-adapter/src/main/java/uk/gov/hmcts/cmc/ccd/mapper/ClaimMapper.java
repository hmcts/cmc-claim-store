package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.DefendantMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.createBigInteger;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class ClaimMapper {

    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthCaseMapper statementOfTruthCaseMapper;
    private final ClaimantMapper claimantMapper;
    private final DefendantMapper defendantMapper;
    private final AmountMapper amountMapper;
    private final PaymentMapper paymentMapper;
    private final InterestMapper interestMapper;
    private final TimelineMapper timelineMapper;
    private final EvidenceMapper evidenceMapper;
    private final MoneyMapper moneyMapper;

    @SuppressWarnings("squid:S00107") //Constructor need all mapper for claim data  mapping
    public ClaimMapper(
        PersonalInjuryMapper personalInjuryMapper,
        HousingDisrepairMapper housingDisrepairMapper,
        StatementOfTruthCaseMapper statementOfTruthCaseMapper,
        ClaimantMapper claimantMapper,
        DefendantMapper defendantMapper,
        AmountMapper amountMapper,
        PaymentMapper paymentMapper,
        InterestMapper interestMapper,
        TimelineMapper timelineMapper,
        EvidenceMapper evidenceMapper,
        MoneyMapper moneyMapper
    ) {
        this.personalInjuryMapper = personalInjuryMapper;
        this.housingDisrepairMapper = housingDisrepairMapper;
        this.statementOfTruthCaseMapper = statementOfTruthCaseMapper;
        this.claimantMapper = claimantMapper;
        this.defendantMapper = defendantMapper;
        this.amountMapper = amountMapper;
        this.paymentMapper = paymentMapper;
        this.interestMapper = interestMapper;
        this.timelineMapper = timelineMapper;
        this.evidenceMapper = evidenceMapper;
        this.moneyMapper = moneyMapper;
    }

    public void to(Claim claim, CCDCase.CCDCaseBuilder builder) {
        ClaimData claimData = claim.getClaimData();
        Objects.requireNonNull(claimData, "claimData must not be null");

        claimData.getFeeCode().ifPresent(builder::feeCode);
        claimData.getFeeAccountNumber().ifPresent(builder::feeAccountNumber);
        claimData.getExternalReferenceNumber().ifPresent(builder::externalReferenceNumber);
        claimData.getPreferredCourt().ifPresent(builder::preferredCourt);

        claimData.getStatementOfTruth()
            .ifPresent(statementOfTruth -> statementOfTruthCaseMapper.to(statementOfTruth, builder));

        claimData.getPersonalInjury().ifPresent(personalInjury -> personalInjuryMapper.to(personalInjury, builder));

        claimData.getHousingDisrepair()
            .ifPresent(housingDisrepair -> housingDisrepairMapper.to(housingDisrepair, builder));

        AtomicInteger applicantIndex = new AtomicInteger(0);
        builder.applicants(claimData.getClaimants().stream()
            .map(claimant -> claimantMapper.to(
                claimant,
                claim,
                isLeadApplicant(claim, applicantIndex.getAndIncrement())
                )
            )
            .collect(Collectors.toList()));

        builder.respondents(claimData.getDefendants().stream()
            .map(defendant -> defendantMapper.to(defendant, claim))
            .collect(Collectors.toList()));

        claimData.getTimeline().ifPresent(timeline -> timelineMapper.to(timeline, builder));
        claimData.getEvidence().ifPresent(evidence -> evidenceMapper.to(evidence, builder));
        claimData.getPayment().ifPresent(payment -> paymentMapper.to(payment, builder));

        interestMapper.to(claimData.getInterest(), builder);
        amountMapper.to(claimData.getAmount(), builder);
        claim.getTotalAmountTillDateOfIssue().map(moneyMapper::to).ifPresent(builder::totalAmount);

        claimData.getFeeAmountInPennies()
            .map(BigInteger::toString)
            .ifPresent(builder::feeAmountInPennies);

        builder
            .reason(claimData.getReason());
    }

    private boolean isLeadApplicant(Claim claim, int applicantIndex) {
        return !claim.getClaimData().isClaimantRepresented() && applicantIndex == 0;
    }

    public void from(CCDCase ccdCase, Claim.ClaimBuilder claimBuilder) {
        Objects.requireNonNull(ccdCase, "ccdCase must not be null");

        List<Party> claimants = asStream(ccdCase.getApplicants())
            .map(claimantMapper::from)
            .collect(Collectors.toList());
        claimBuilder.claimData(
            new ClaimData(
                UUID.fromString(ccdCase.getExternalId()),
                claimants,
                getDefendants(ccdCase, claimBuilder),
                paymentMapper.from(ccdCase),
                amountMapper.from(ccdCase),
                createBigInteger(ccdCase.getFeeAmountInPennies()),
                interestMapper.from(ccdCase),
                personalInjuryMapper.from(ccdCase),
                housingDisrepairMapper.from(ccdCase),
                ccdCase.getReason(),
                statementOfTruthCaseMapper.from(ccdCase),
                ccdCase.getFeeAccountNumber(),
                ccdCase.getExternalReferenceNumber(),
                ccdCase.getPreferredCourt(),
                ccdCase.getFeeCode(),
                timelineMapper.from(ccdCase),
                evidenceMapper.from(ccdCase)
            )
        );
    }

    private List<TheirDetails> getDefendants(CCDCase ccdCase, Claim.ClaimBuilder claimBuilder) {

        return asStream(ccdCase.getRespondents())
            .map(respondent -> defendantMapper.from(claimBuilder, respondent))
            .collect(Collectors.toList());
    }
}
