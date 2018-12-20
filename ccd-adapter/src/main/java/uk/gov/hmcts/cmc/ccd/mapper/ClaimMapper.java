package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.AmountType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.DefendantMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
        EvidenceMapper evidenceMapper
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

        builder.claimants(claimData.getClaimants().stream().map(claimantMapper::to)
            .map(this::mapClaimantToValue)
            .collect(Collectors.toList()));

        // For legal, we expect more than one claimants
        if (claimData.getAmount() instanceof AmountBreakDown) {
            builder.defendants(claimData.getDefendants().stream()
                .map(ccdDefendant -> defendantMapper.to(ccdDefendant, claim))
                .map(this::mapDefendantToValue)
                .collect(Collectors.toList()));
        } else {
            builder.defendants(claimData.getDefendants().stream()
                .map(defendantMapper::to)
                .map(this::mapDefendantToValue)
                .collect(Collectors.toList()));
        }

        claimData.getTimeline().ifPresent(timeline -> timelineMapper.to(timeline, builder));

        claimData.getEvidence().ifPresent(evidence -> evidenceMapper.to(evidence, builder));

        paymentMapper.to(claimData.getPayment(), builder);
        interestMapper.to(claimData.getInterest(), builder);
        amountMapper.to(claimData.getAmount(), builder);

        builder
            .reason(claimData.getReason())
            .feeAmountInPennies(claimData.getFeeAmountInPennies());
    }

    private CCDCollectionElement<CCDClaimant> mapClaimantToValue(CCDClaimant ccdParty) {
        return CCDCollectionElement.<CCDClaimant>builder().value(ccdParty).build();
    }

    private CCDCollectionElement<CCDDefendant> mapDefendantToValue(CCDDefendant ccdParty) {
        return CCDCollectionElement.<CCDDefendant>builder().value(ccdParty).build();
    }

    public void from(CCDCase ccdCase, Claim.ClaimBuilder claimBuilder) {
        Objects.requireNonNull(ccdCase, "ccdClaim must not be null");

        List<Party> claimants = ccdCase.getClaimants()
            .stream()
            .map(CCDCollectionElement::getValue)
            .map(claimantMapper::from)
            .collect(Collectors.toList());

        claimBuilder.claimData(
            new ClaimData(
                UUID.fromString(ccdCase.getExternalId()),
                claimants,
                getDefendants(ccdCase, claimBuilder),
                paymentMapper.from(ccdCase),
                amountMapper.from(ccdCase),
                ccdCase.getFeeAmountInPennies(),
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

        if (ccdCase.getAmountType() == AmountType.BREAK_DOWN) {
            CCDDefendant ccdDefendant = ccdCase.getDefendants().stream()
                .map(CCDCollectionElement::getValue)
                .findFirst()
                .orElseThrow(() -> new MappingException("No defendant mapped from case"));

            return Collections.singletonList(defendantMapper.from(claimBuilder, ccdDefendant));
        } else {
            return ccdCase.getDefendants().stream()
                .map(CCDCollectionElement::getValue)
                .map(defendantMapper::from)
                .collect(Collectors.toList());
        }
    }
}
