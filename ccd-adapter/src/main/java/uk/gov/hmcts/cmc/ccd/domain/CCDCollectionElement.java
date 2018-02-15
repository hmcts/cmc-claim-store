package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDCollectionElement<T> {
    private String id;
    private T value;
}
