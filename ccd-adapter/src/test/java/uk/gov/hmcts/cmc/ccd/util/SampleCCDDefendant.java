package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType.ALREADY_PAID;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType.OTHER;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDAddress;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPaymentIntention;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDStatementOfMeans;

public class SampleCCDDefendant {

    private SampleCCDDefendant() {
        //Utility class
    }

    public static CCDDefendant.CCDDefendantBuilder withDefault() {
        return CCDDefendant.builder()
            .claimantProvidedType(INDIVIDUAL)
            .defendantId("defendantId")
            .letterHolderId("JCJEDU")
            .responseDeadline(now().plusDays(14))
            .partyEmail("defendant@Ididabadjob.com");
    }

    public static CCDDefendant.CCDDefendantBuilder withResponseMoreTimeNeededOption() {
        return withDefault().responseMoreTimeNeededOption(NO);
    }

    private static CCDDefendant.CCDDefendantBuilder withParty() {
        return CCDDefendant.builder()
            .partyType(COMPANY)
            .partyName("Mr Norman")
            .partyAddress(getCCDAddress())
            .partyCorrespondenceAddress(getCCDAddress())
            .partyPhone("07123456789")
            .representativeOrganisationName("Trading ltd")
            .representativeOrganisationAddress(getCCDAddress())
            .representativeOrganisationPhone("07123456789")
            .representativeOrganisationEmail("representative@example.org")
            .representativeOrganisationDxAddress("DX123456");
    }

    public static CCDDefendant.CCDDefendantBuilder withPartyIndividual() {
        return withParty()
            .partyDateOfBirth(LocalDate.of(1980, 1, 1));
    }

    public static CCDDefendant.CCDDefendantBuilder withPartyCompany() {
        return withParty()
            .partyContactPerson("Mr Steven");
    }

    public static CCDDefendant.CCDDefendantBuilder withPartySoleTrader() {
        return withParty()
            .partyTitle("Mr")
            .partyBusinessName("Trading as name");
    }

    public static CCDDefendant.CCDDefendantBuilder withPartyOrganisation() {
        return withParty()
            .partyContactPerson("Mr Steven")
            .partyCompaniesHouseNumber("12345");
    }

    private static CCDDefendant.CCDDefendantBuilder withResponse() {
        return withPartyIndividual()
            .responseMoreTimeNeededOption(NO)
            .responseFreeMediationOption(NO)
            .responseDefendantSOTSignerName("Signer name")
            .responseDefendantSOTSignerRole("Signer role");
    }

    public static CCDDefendant.CCDDefendantBuilder withFullDefenceResponse() {
        return withResponse()
            .responseType(FULL_DEFENCE)
            .responseDefenceType(ALREADY_PAID)
            .responseDefence("This is my defence")
            .paymentDeclarationPaidDate(now())
            .paymentDeclarationExplanation("Payment declaration explanation")
            .defendantTimeLineComment("Time line comments")
            .defendantTimeLineEvents(asList(
                CCDCollectionElement.<CCDTimelineEvent>builder().value(CCDTimelineEvent.builder()
                    .date("Time of event")
                    .description("Description of the event")
                    .build()
                ).build()
            ))
            .responseEvidenceComment("Evidence comments")
            .responseEvidenceRows(
                asList(
                    CCDCollectionElement.<CCDEvidenceRow>builder().value(CCDEvidenceRow.builder()
                        .type(OTHER)
                        .description("My description")
                        .build()
                    ).build()
                ));
    }

    public static CCDDefendant.CCDDefendantBuilder withFullAdmissionResponse() {
        return withResponse()
            .responseType(FULL_ADMISSION)
            .statementOfMeans(getCCDStatementOfMeans())
            .defendantPaymentIntention(getCCDPaymentIntention());
    }

    public static CCDDefendant.CCDDefendantBuilder withPartAdmissionResponse() {
        return withResponse()
            .responseType(PART_ADMISSION)
            .responseAmount(TEN)
            .paymentDeclarationPaidDate(now())
            .paymentDeclarationExplanation("Payment declaration explanation")
            .defendantPaymentIntention(getCCDPaymentIntention())
            .responseDefence("This is my defence")
            .statementOfMeans(getCCDStatementOfMeans())
            .defendantTimeLineComment("Time line comments")
            .defendantTimeLineEvents(asList(
                CCDCollectionElement.<CCDTimelineEvent>builder().value(CCDTimelineEvent.builder()
                    .date("Time of event")
                    .description("Description of the event")
                    .build()
                ).build()
            ))
            .responseEvidenceComment("Evidence comments")
            .responseEvidenceRows(
                asList(
                    CCDCollectionElement.<CCDEvidenceRow>builder().value(CCDEvidenceRow.builder()
                        .type(OTHER)
                        .description("My description")
                        .build()
                    ).build()
                ));
    }

    public static CCDDefendant.CCDDefendantBuilder withPartyStatements() {
        List<CCDCollectionElement<CCDPartyStatement>> partyStatements =
            asList(
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.offerPartyStatement()).build(),
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.acceptPartyStatement()).build(),
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.counterSignPartyStatement()).build()
            );
        return withPartAdmissionResponse().settlementReachedAt(LocalDateTime.now())
            .settlementPartyStatements(partyStatements);
    }
}
