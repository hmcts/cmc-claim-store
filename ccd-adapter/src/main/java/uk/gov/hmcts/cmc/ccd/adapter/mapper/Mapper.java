package uk.gov.hmcts.cmc.ccd.adapter.mapper;

public interface Mapper<K, V> {

    K to(V v);

    V from(K k);
}
