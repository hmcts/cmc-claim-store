package uk.gov.hmcts.cmc.dm.batch.db;

public interface Query {

    String SEALED_CLAIM_COUNT = "select count(id) from claim where "
                        + "sealed_claim_document_management_self_path is null";
}
