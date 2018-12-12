package uk.gov.hmcts.cmc.ccd.domain.mapper;

public interface Mapper<K, V> {

    K to(V v);

    V from(K k);
}
