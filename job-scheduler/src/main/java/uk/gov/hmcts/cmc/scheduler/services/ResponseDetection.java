package uk.gov.hmcts.cmc.scheduler.services;

public interface ResponseDetection {
    boolean isAlreadyResponded(String caseId, String defendantId);
}
