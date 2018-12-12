package uk.gov.hmcts.cmc.domain.models.evidence;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum EvidenceType {
    CONTRACTS_AND_AGREEMENTS("Contracts and agreements"),
    EXPERT_WITNESS("Expert witness"),
    CORRESPONDENCE("Letters, emails and other correspondence"),
    PHOTO("Photo evidence"),
    RECEIPTS("Receipts"),
    STATEMENT_OF_ACCOUNT("Statements of account"),
    OTHER("Other");

    private static final Map<String, EvidenceType> ENUM_MAP;
    String description;

    EvidenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    static {
        Map<String, EvidenceType> map = new ConcurrentHashMap<String, EvidenceType>();
        for (EvidenceType instance : EvidenceType.values()) {
            map.put(instance.name(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static EvidenceType get(String name) {
        return ENUM_MAP.get(name);
    }
}
