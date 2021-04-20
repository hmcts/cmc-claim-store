package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDBreathingSpaceType {
    STANDARD_BS_ENTERED("Standard Breathing Space Entered", 720),
    MENTAL_BS_ENTERED("Mental Health Crises Moratorium Entered", 723),
    STANDARD_BS_LIFTED("Standard Breathing Space Lifted", 721),
    MENTAL_BS_LIFTED("Mental Health Crises Moratorium Lifted", 724);

    private final String key;
    private final Integer value;

    CCDBreathingSpaceType(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}
